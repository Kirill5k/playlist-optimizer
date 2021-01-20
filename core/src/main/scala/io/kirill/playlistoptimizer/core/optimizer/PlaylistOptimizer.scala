package io.kirill.playlistoptimizer.core.optimizer

import cats.effect.concurrent.Ref
import cats.effect.implicits._
import cats.effect.{Concurrent, ContextShift, Timer}
import cats.implicits._
import fs2.Stream
import io.kirill.playlistoptimizer.core.common.controllers.AppController.UserSessionId
import io.kirill.playlistoptimizer.core.common.errors.OptimizationNotFound
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.{Playlist, Track}

import java.time.Instant
import scala.concurrent.duration._
import scala.util.Random

trait PlaylistOptimizer[F[_]] {
  def optimize(uid: UserSessionId, playlist: Playlist, parameters: OptimizationParameters): F[OptimizationId]
  def get(uid: UserSessionId, id: OptimizationId): F[Optimization[Playlist]]
  def getAll(uid: UserSessionId): F[List[Optimization[Playlist]]]
  def delete(uid: UserSessionId, id: OptimizationId): F[Unit]
}

private class InmemoryPlaylistOptimizer[F[_]: Concurrent: ContextShift](
    private val state: Ref[F, Map[UserSessionId, Map[OptimizationId, Optimization[Playlist]]]]
)(
    implicit val alg: OptimizationAlgorithm[F, Track]
) extends PlaylistOptimizer[F] {

  implicit val r: Random = new Random()

  override def get(uid: UserSessionId, oid: OptimizationId): F[Optimization[Playlist]] =
    state.get
      .map(_.get(uid).fold[Option[Optimization[Playlist]]](None)(_.get(oid)))
      .flatMap {
        case None      => OptimizationNotFound(oid).raiseError[F, Optimization[Playlist]]
        case Some(opt) => opt.pure[F]
      }

  override def getAll(uid: UserSessionId): F[List[Optimization[Playlist]]] =
    state.get.map(_.get(uid).fold(List.empty[Optimization[Playlist]])(_.values.toList))

  override def optimize(uid: UserSessionId, playlist: Playlist, parameters: OptimizationParameters): F[OptimizationId] =
    for {
      oid <- save(uid, Optimization.init[Playlist](parameters, playlist))
      process = alg.optimizeSeq(playlist.tracks, parameters).flatMap(res => complete(uid, oid, res._1, res._2))
      _ <- process.start.void
    } yield oid

  private def complete(uid: UserSessionId, id: OptimizationId, result: IndexedSeq[Track], score: BigDecimal): F[Unit] =
    for {
      opt <- get(uid, id)
      optimizedPlaylist = opt.original.copy(name = s"${opt.original.name} optimized", tracks = result)
      duration          = (Instant.now().toEpochMilli - opt.dateInitiated.toEpochMilli).millis
      _ <- save(uid, opt.complete(optimizedPlaylist, score, duration))
    } yield ()

  private def save(uid: UserSessionId, opt: Optimization[Playlist]): F[OptimizationId] =
    state.update(s => s + (uid -> (s.getOrElse(uid, Map()) + (opt.id -> opt)))) *>
      opt.id.pure[F]

  override def delete(uid: UserSessionId, id: OptimizationId): F[Unit] =
    state.get.flatMap {
      case s if s.get(uid).exists(_.contains(id)) => state.update(curr => curr + (uid -> curr(uid).removed(id)))
      case _                                      => OptimizationNotFound(id).raiseError[F, Unit]
    }
}

object PlaylistOptimizer {

  def inmemoryPlaylistOptimizer[F[_]: Concurrent: ContextShift: Timer](
      expiresIn: FiniteDuration = 24.hours,
      checkOnExpirationsEvery: FiniteDuration = 15.minutes
  )(
      implicit alg: OptimizationAlgorithm[F, Track]
  ): F[PlaylistOptimizer[F]] = {
    def runExpiration(state: Ref[F, Map[UserSessionId, Map[OptimizationId, Optimization[Playlist]]]]): F[Unit] = {
      Stream.repeatEval {
        state.update(_.foldLeft(Map.empty[UserSessionId, Map[OptimizationId, Optimization[Playlist]]]) {
          case (res, (uid, optsMap)) =>
            res + (uid -> optsMap.filter {
              case (_, opt) => opt.duration.fold(true)(d => opt.dateInitiated.plusNanos(d.toNanos + expiresIn.toNanos).isAfter(Instant.now))
            })
        })
      }
        .metered(checkOnExpirationsEvery)
        .compile
        .drain
    }

    Ref
      .of[F, Map[UserSessionId, Map[OptimizationId, Optimization[Playlist]]]](Map())
      .flatTap(s => runExpiration(s).start.void)
      .map(s => new InmemoryPlaylistOptimizer(s))
  }
}

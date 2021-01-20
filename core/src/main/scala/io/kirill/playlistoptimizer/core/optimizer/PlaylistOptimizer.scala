package io.kirill.playlistoptimizer.core.optimizer

import java.time.Instant
import java.util.UUID

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ContextShift, Sync, Timer}
import cats.implicits._
import cats.effect.implicits._
import io.kirill.playlistoptimizer.core.common.controllers.AppController.UserSessionId
import io.kirill.playlistoptimizer.core.common.errors.OptimizationNotFound
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.{Playlist, Track}

import scala.concurrent.duration._
import scala.util.Random

trait PlaylistOptimizer[F[_]] {
  def optimize(userId: UserSessionId, playlist: Playlist, parameters: OptimizationParameters): F[OptimizationId]
  def get(userId: UserSessionId, id: OptimizationId): F[Optimization[Playlist]]
  def getAll(userId: UserSessionId): F[List[Optimization[Playlist]]]
  def delete(userId: UserSessionId, id: OptimizationId): F[Unit]
}

private class InmemoryPlaylistOptimizer[F[_]: Concurrent: ContextShift](
    private val state: Ref[F, Map[UserSessionId, Map[OptimizationId, Optimization[Playlist]]]]
)(
    implicit val alg: OptimizationAlgorithm[F, Track]
) extends PlaylistOptimizer[F] {

  implicit val r: Random = new Random()

  override def get(userId: UserSessionId, id: OptimizationId): F[Optimization[Playlist]] =
    state.get
      .map(_.get(userId).fold[Option[Optimization[Playlist]]](None)(_.get(id)))
      .flatMap {
        case None      => OptimizationNotFound(id).raiseError[F, Optimization[Playlist]]
        case Some(opt) => opt.pure[F]
      }

  override def getAll(userId: UserSessionId): F[List[Optimization[Playlist]]] =
    state.get.map(_.get(userId).fold[List[Optimization[Playlist]]](Nil)(_.values.toList))

  override def optimize(userId: UserSessionId, playlist: Playlist, parameters: OptimizationParameters): F[OptimizationId] =
    for {
      id <- Sync[F].delay(OptimizationId(UUID.randomUUID()))
      _  <- save(userId, id, Optimization.init[Playlist](id, parameters, playlist))
      process = alg.optimizeSeq(playlist.tracks, parameters).flatMap(res => complete(userId, id, res._1, res._2))
      _ <- process.start.void
    } yield id

  private def complete(userId: UserSessionId, id: OptimizationId, result: IndexedSeq[Track], score: BigDecimal): F[Unit] =
    for {
      opt <- get(userId, id)
      optimizedPlaylist = opt.original.copy(name = s"${opt.original.name} optimized", tracks = result)
      duration          = (Instant.now().toEpochMilli - opt.dateInitiated.toEpochMilli).millis
      _ <- save(userId, id, opt.complete(optimizedPlaylist, score, duration))
    } yield ()

  private def save(userId: UserSessionId, id: OptimizationId, optimization: Optimization[Playlist]): F[Unit] =
    state.update(s => s + (userId -> (s.getOrElse(userId, Map()) + (id -> optimization))))

  override def delete(userId: UserSessionId, id: OptimizationId): F[Unit] =
    state.get.flatMap {
      case s if s.get(userId).exists(_.contains(id)) => state.update(curr => curr + (userId -> curr(userId).removed(id)))
      case _                                         => OptimizationNotFound(id).raiseError[F, Unit]
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
      val process = state.update(_.foldLeft(Map.empty[UserSessionId, Map[OptimizationId, Optimization[Playlist]]]) {
        case (res, (uid, optsMap)) =>
          res + (uid -> optsMap.filter {
            case (_, opt) => opt.duration.fold(true)(d => opt.dateInitiated.plusNanos(d.toNanos + expiresIn.toNanos).isAfter(Instant.now))
          })
      })
      Timer[F].sleep(checkOnExpirationsEvery) >> process >> runExpiration(state)
    }

    Ref
      .of[F, Map[UserSessionId, Map[OptimizationId, Optimization[Playlist]]]](Map())
      .flatTap(s => Concurrent[F].start(runExpiration(s)).void)
      .map(s => new InmemoryPlaylistOptimizer(s))
  }
}

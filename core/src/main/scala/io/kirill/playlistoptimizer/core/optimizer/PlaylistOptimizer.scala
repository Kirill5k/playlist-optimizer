package io.kirill.playlistoptimizer.core.optimizer

import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ContextShift, Sync, Timer}
import cats.implicits._
import io.kirill.playlistoptimizer.core.common.controllers.AppController.UserSessionId
import io.kirill.playlistoptimizer.core.common.errors.OptimizationNotFound
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.{Playlist, Track}

import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.Random

trait PlaylistOptimizer[F[_]] {
  def optimize(userId: UserSessionId, playlist: Playlist, parameters: OptimizationParameters): F[OptimizationId]
  def get(userId: UserSessionId, id: OptimizationId): F[Optimization]
  def getAll(userId: UserSessionId): F[List[Optimization]]
  def delete(userId: UserSessionId, id: OptimizationId): F[Unit]
}

private class InmemoryPlaylistOptimizer[F[_]: Concurrent: ContextShift](
    private val state: Ref[F, Map[UserSessionId, Map[OptimizationId, Optimization]]]
)(
    implicit val alg: OptimizationAlgorithm[F, Track]
) extends PlaylistOptimizer[F] {

  implicit val r: Random = new Random()

  override def get(userId: UserSessionId, id: OptimizationId): F[Optimization] =
    state.get
      .map(_.get(userId).fold[Option[Optimization]](None)(_.get(id)))
      .flatMap {
        case None      => Sync[F].raiseError(OptimizationNotFound(id))
        case Some(opt) => Sync[F].pure(opt)
      }

  override def getAll(userId: UserSessionId): F[List[Optimization]] =
    state.get.map(_.get(userId).fold[List[Optimization]](Nil)(_.values.toList))

  override def optimize(userId: UserSessionId, playlist: Playlist, parameters: OptimizationParameters): F[OptimizationId] =
    for {
      id <- Sync[F].delay(OptimizationId(UUID.randomUUID()))
      _  <- updateOptInState(userId, id, Optimization(id, "in progress", parameters, playlist, Instant.now()))
      process = alg.optimizeSeq(playlist.tracks, parameters).flatMap(res => updateState(userId, id, res._1, res._2))
      _ <- Concurrent[F].start(process).void
    } yield id

  private def updateState(userId: UserSessionId, id: OptimizationId, result: IndexedSeq[Track], score: BigDecimal): F[Unit] =
    for {
      opt <- get(userId, id)
      optimizedPlaylist = opt.original.copy(name = s"${opt.original.name} optimized", tracks = result)
      duration          = (Instant.now().toEpochMilli - opt.dateInitiated.toEpochMilli).millis
      completedOpt      = opt.copy(status = "completed", duration = Some(duration), result = Some(optimizedPlaylist), score = Some(score))
      _ <- updateOptInState(userId, id, completedOpt)
    } yield ()

  private def updateOptInState(userId: UserSessionId, id: OptimizationId, optimization: Optimization): F[Unit] =
    state.update(s => s + (userId -> (s.getOrElse(userId, Map()) + (id -> optimization))))

  override def delete(userId: UserSessionId, id: OptimizationId): F[Unit] =
    state.get.flatMap {
      case s if s.get(userId).exists(_.contains(id)) => state.update(curr => curr + (userId -> (curr(userId) - id)))
      case _                                         => Sync[F].raiseError(OptimizationNotFound(id))
    }
}

object PlaylistOptimizer {

  def inmemoryPlaylistOptimizer[F[_]: Concurrent: ContextShift: Timer](
      expiresIn: FiniteDuration = 24.hours,
      checkOnExpirationsEvery: FiniteDuration = 15.minutes
  )(
      implicit alg: OptimizationAlgorithm[F, Track]
  ): F[PlaylistOptimizer[F]] = {
    def runExpiration(state: Ref[F, Map[UserSessionId, Map[OptimizationId, Optimization]]]): F[Unit] = {
      val process = state.update(_.foldLeft(Map.empty[UserSessionId, Map[OptimizationId, Optimization]]) {
        case (res, (uid, optsMap)) =>
          res + (uid -> optsMap.filter {
            case (_, opt) => opt.duration.fold(true)(d => opt.dateInitiated.plusNanos(d.toNanos + expiresIn.toNanos).isAfter(Instant.now))
          })
      })
      Timer[F].sleep(checkOnExpirationsEvery) >> process >> runExpiration(state)
    }

    Ref
      .of[F, Map[UserSessionId, Map[OptimizationId, Optimization]]](Map())
      .flatTap(s => Concurrent[F].start(runExpiration(s)).void)
      .map(s => new InmemoryPlaylistOptimizer(s))
  }
}

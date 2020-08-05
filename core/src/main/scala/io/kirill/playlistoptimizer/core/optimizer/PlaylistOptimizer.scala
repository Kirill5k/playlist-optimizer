package io.kirill.playlistoptimizer.core.optimizer

import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ContextShift, Sync}
import cats.implicits._
import io.kirill.playlistoptimizer.core.common.errors.OptimizationNotFound
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.{Playlist, Track}

import scala.concurrent.duration.FiniteDuration

trait PlaylistOptimizer[F[_]] {
  def optimize(playlist: Playlist, parameters: OptimizationParameters): F[OptimizationId]
  def get(id: OptimizationId): F[Optimization]
  def getAll(): F[List[Optimization]]
  def delete(id: OptimizationId): F[Unit]
}

private class RefBasedPlaylistOptimizer[F[_]: Concurrent: ContextShift](
    private val state: Ref[F, Map[OptimizationId, Optimization]]
)(
    implicit val alg: OptimizationAlgorithm[F, Track]
) extends PlaylistOptimizer[F] {

  override def get(id: OptimizationId): F[Optimization] =
    state.get
      .map(_.get(id))
      .flatMap {
        case None      => Sync[F].raiseError(OptimizationNotFound(id))
        case Some(opt) => Sync[F].pure(opt)
      }

  override def getAll(): F[List[Optimization]] =
    state.get.map(_.values.toList)

  override def optimize(playlist: Playlist, parameters: OptimizationParameters): F[OptimizationId] =
    for {
      id <- Sync[F].delay(OptimizationId(UUID.randomUUID()))
      _  <- state.update(s => s + (id -> Optimization(id, "in progress", playlist, Instant.now())))
      _  <- Concurrent[F].start(alg.optimizeSeq(playlist.tracks).flatMap(res => updateState(id, res._1, res._2))).void
    } yield id

  private def updateState(id: OptimizationId, result: Seq[Track], score: Double): F[Unit] =
    for {
      opt <- get(id)
      optimizedPlaylist = opt.original.copy(name = s"${opt.original.name} optimized", tracks = result)
      duration          = FiniteDuration(Instant.now().toEpochMilli - opt.dateInitiated.toEpochMilli, TimeUnit.MILLISECONDS)
      completedOpt      = opt.copy(status = "completed", duration = Some(duration), result = Some(optimizedPlaylist), score = Some(score))
      _ <- state.update(_ + (id -> completedOpt))
    } yield ()

  override def delete(id: OptimizationId): F[Unit] =
    state.get.flatMap {
      case s if s.contains(id) => state.update(_ - id)
      case _                   => Sync[F].raiseError(OptimizationNotFound(id))
    }
}

object PlaylistOptimizer {

  def refBasedPlaylistOptimizer[F[_]: Concurrent: ContextShift](
      implicit alg: OptimizationAlgorithm[F, Track]
  ): F[PlaylistOptimizer[F]] =
    Ref
      .of[F, Map[OptimizationId, Optimization]](Map())
      .map(s => new RefBasedPlaylistOptimizer(s))
}

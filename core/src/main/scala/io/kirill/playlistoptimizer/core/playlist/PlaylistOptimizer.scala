package io.kirill.playlistoptimizer.core.playlist

import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ContextShift, Sync}
import cats.implicits._
import io.kirill.playlistoptimizer.core.common.errors.OptimizationNotFound
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.PlaylistOptimizer.{Optimization, OptimizationId}

import scala.concurrent.duration.FiniteDuration

trait PlaylistOptimizer[F[_]] {
  def optimize(playlist: Playlist): F[OptimizationId]

  def get(id: OptimizationId): F[Optimization]
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

  override def optimize(playlist: Playlist): F[OptimizationId] =
    for {
      id <- Sync[F].delay(OptimizationId(UUID.randomUUID()))
      _  <- state.update(s => s + (id -> Optimization(id, playlist, Instant.now())))
      _  <- Concurrent[F].start(alg.optimizeSeq(playlist.tracks).flatMap(res => updateState(id, res))).void
    } yield id

  private def updateState(id: OptimizationId, result: Seq[Track]): F[Unit] =
    for {
      opt <- get(id)
      optimizedPlaylist = opt.original.copy(name = s"${opt.original.name} optimized", tracks = result)
      duration          = FiniteDuration(Instant.now().toEpochMilli - opt.dateInitiated.toEpochMilli, TimeUnit.MILLISECONDS)
      completedOpt      = opt.copy(duration = Some(duration), result = Some(optimizedPlaylist))
      _ <- state.update(s => s + (id -> completedOpt))
    } yield ()
}

object PlaylistOptimizer {
  final case class OptimizationId(value: UUID) extends AnyVal

  final case class Optimization(
      id: OptimizationId,
      original: Playlist,
      dateInitiated: Instant,
      duration: Option[FiniteDuration] = None,
      result: Option[Playlist] = None
  )

  def refBasedPlaylistOptimizer[F[_]: Concurrent: ContextShift](
      implicit alg: OptimizationAlgorithm[F, Track]
  ): F[PlaylistOptimizer[F]] =
    Ref
      .of[F, Map[OptimizationId, Optimization]](Map())
      .map(s => new RefBasedPlaylistOptimizer(s))
}

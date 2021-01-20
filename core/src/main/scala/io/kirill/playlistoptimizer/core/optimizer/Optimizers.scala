package io.kirill.playlistoptimizer.core.optimizer

import cats.effect.{Concurrent, ContextShift, Timer}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.Track

final class Optimizers[F[_]](
    val optimizationController: OptimizationController[F]
)

object Optimizers {
  def playlist[F[_]: Concurrent: Logger: ContextShift: Timer](
      implicit alg: OptimizationAlgorithm[F, Track]
  ): F[Optimizers[F]] =
    for {
      playlistOptimizer <- Optimizer.inmemoryPlaylistOptimizer[F]()
      controller        <- OptimizationController.make(playlistOptimizer)
    } yield new Optimizers(controller)
}

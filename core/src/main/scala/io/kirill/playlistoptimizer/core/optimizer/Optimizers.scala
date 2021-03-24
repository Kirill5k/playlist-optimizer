package io.kirill.playlistoptimizer.core.optimizer

import cats.effect.{Concurrent, Timer}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.Track

final class Optimizers[F[_]](
    val optimizationController: OptimizationController[F]
)

object Optimizers {
  def playlist[F[_]: Concurrent: Logger: Timer](
      alg: OptimizationAlgorithm[F, Track]
  ): F[Optimizers[F]] =
    for {
      playlistOptimizer <- Optimizer.inmemoryPlaylistOptimizer[F](alg)
      controller        <- OptimizationController.make(playlistOptimizer)
    } yield new Optimizers(controller)
}

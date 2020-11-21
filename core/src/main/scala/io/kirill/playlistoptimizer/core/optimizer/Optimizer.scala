package io.kirill.playlistoptimizer.core.optimizer

import cats.effect.{Concurrent, ContextShift, Timer}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.Track

final class Optimizer[F[_]](
    val optimizationController: OptimizationController[F]
)

object Optimizer {
  def make[F[_]: Concurrent: Logger: ContextShift: Timer](
      implicit alg: OptimizationAlgorithm[F, Track]
  ): F[Optimizer[F]] =
    for {
      playlistOptimizer <- PlaylistOptimizer.inmemoryPlaylistOptimizer[F]()
      controller        <- OptimizationController.make(playlistOptimizer)
    } yield new Optimizer(controller)
}

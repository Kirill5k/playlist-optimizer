package io.kirill.playlistoptimizer.core

import cats.effect.{Concurrent, ContextShift}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.Track

package object optimizer {

  final class Optimizer[F[_]](
      val optimizationController: OptimizationController[F]
  )

  object Optimizer {
    def make[F[_]: Concurrent: Logger: ContextShift](
        implicit alg: OptimizationAlgorithm[F, Track]
    ): F[Optimizer[F]] =
      for {
        playlistOptimizer <- PlaylistOptimizer.refBasedPlaylistOptimizer[F]
        controller        <- OptimizationController.make(playlistOptimizer)
      } yield new Optimizer(controller)
  }
}

package io.kirill.playlistoptimizer.core.optimizer

import cats.effect.{Concurrent, Timer}
import cats.implicits._
import io.kirill.playlistoptimizer.core.common.controllers.Controller
import org.typelevel.log4cats.Logger
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.Track

final case class Optimizers[F[_]](
    controller: Controller[F]
)

object Optimizers {
  def playlist[F[_]: Concurrent: Logger: Timer](
      alg: OptimizationAlgorithm[F, Track]
  ): F[Optimizers[F]] =
    for {
      playlistOptimizer <- Optimizer.inmemoryPlaylistOptimizer[F](alg)
      controller        <- OptimizationController.make(playlistOptimizer)
    } yield Optimizers(controller)
}

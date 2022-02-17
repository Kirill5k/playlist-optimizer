package io.kirill.playlistoptimizer.core.optimizer

import cats.effect.Async
import cats.implicits.*
import io.kirill.playlistoptimizer.core.common.controllers.Controller
import org.typelevel.log4cats.Logger
import io.kirill.playlistoptimizer.domain.playlist.Track

trait Optimizers[F[_]]:
  def controller: Controller[F]

object Optimizers:
  def playlist[F[_]: Async: Logger](alg: OptimizationAlgorithm[F, Track]): F[Optimizers[F]] =
    Optimizer
      .inmemoryPlaylistOptimizer[F](alg)
      .flatMap(OptimizationController.make)
      .map { optimizationController =>
        new Optimizers[F] {
          def controller: Controller[F] = optimizationController
        }
      }

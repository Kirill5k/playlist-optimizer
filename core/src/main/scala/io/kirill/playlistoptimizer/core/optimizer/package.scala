package io.kirill.playlistoptimizer.core

import java.time.Instant
import java.util.UUID

import cats.effect.{Concurrent, ContextShift}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.{Playlist, Track}

import scala.concurrent.duration.FiniteDuration

package object optimizer {

  final case class OptimizationParameters(
      populationSize: Int,
      maxGen: Int,
      crossoverProbability: Double,
      mutationProbability: Double,
      elitismRatio: Double,
      shuffle: Boolean
  )

  final case class OptimizationId(value: UUID) extends AnyVal

  final case class Optimization(
      id: OptimizationId,
      status: String,
      parameters: OptimizationParameters,
      original: Playlist,
      dateInitiated: Instant,
      duration: Option[FiniteDuration] = None,
      result: Option[Playlist] = None,
      score: Option[Double] = None
  )

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

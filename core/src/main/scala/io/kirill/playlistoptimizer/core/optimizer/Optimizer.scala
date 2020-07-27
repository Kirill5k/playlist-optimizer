package io.kirill.playlistoptimizer.core.optimizer

import java.time.Instant
import java.util.UUID

import io.kirill.playlistoptimizer.core.optimizer.Optimizer.{OptimizationId, OptimizationResult}
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm

import scala.concurrent.duration.FiniteDuration

trait Optimizer[F[_], A] {
  def optimize(item: A)(implicit alg: OptimizationAlgorithm[F, A]): F[OptimizationId]

  def get(id: UUID): F[OptimizationResult[A]]
}

object Optimizer {
  final case class OptimizationId(value: UUID) extends AnyVal

  final case class OptimizationResult[A](
      id: OptimizationId,
      dateInitiated: Instant,
      duration: FiniteDuration,
      result: Option[A]
  )
}

package io.kirill.playlistoptimizer.core.optimizer

import java.time.Instant
import java.util.UUID

import cats.effect.Async
import cats.effect.concurrent.Ref
import cats.implicits._
import io.kirill.playlistoptimizer.core.common.errors.OptimizationNotFound
import io.kirill.playlistoptimizer.core.optimizer.Optimizer.{OptimizationId, OptimizationResult}
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm

import scala.concurrent.duration.FiniteDuration

trait Optimizer[F[_], A] {
  def optimize(item: A)(implicit alg: OptimizationAlgorithm[F, A]): F[OptimizationId]

  def get(id: OptimizationId): F[OptimizationResult[A]]
}

private class RefBasedOptimizer[F[_]: Async, A](
    private val state: Ref[F, Map[OptimizationId, OptimizationResult[A]]]
) extends Optimizer[F, A] {

  override def optimize(item: A)(implicit alg: OptimizationAlgorithm[F, A]): F[Optimizer.OptimizationId] = ???

  override def get(id: OptimizationId): F[Optimizer.OptimizationResult[A]] =
    state.get
      .map(_.get(id))
      .flatMap {
        case None => Async[F].raiseError(OptimizationNotFound(id))
        case Some(opt) => Async[F].pure(opt)
      }
}

object Optimizer {
  final case class OptimizationId(value: UUID) extends AnyVal

  final case class OptimizationResult[A](
      id: OptimizationId,
      dateInitiated: Instant,
      duration: Option[FiniteDuration] = None,
      result: Option[A] = None
  )
}

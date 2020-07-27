package io.kirill.playlistoptimizer.core.optimizer

import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

import cats.effect.concurrent.Ref
import cats.effect.{Async, Blocker, Concurrent, ContextShift, Sync}
import cats.implicits._
import io.kirill.playlistoptimizer.core.common.errors.OptimizationNotFound
import io.kirill.playlistoptimizer.core.optimizer.Optimizer.{OptimizationId, OptimizationResult}
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm

import scala.concurrent.duration.FiniteDuration

trait Optimizer[F[_], A] {
  def optimize(items: Seq[A]): F[OptimizationId]

  def get(id: OptimizationId): F[OptimizationResult[A]]
}

private class RefBasedOptimizer[F[_]: Concurrent: ContextShift, A](
    private val state: Ref[F, Map[OptimizationId, OptimizationResult[A]]]
)(
    implicit val alg: OptimizationAlgorithm[F, A]
) extends Optimizer[F, A] {

  override def get(id: OptimizationId): F[Optimizer.OptimizationResult[A]] =
    state.get
      .map(_.get(id))
      .flatMap {
        case None      => Sync[F].raiseError(OptimizationNotFound(id))
        case Some(opt) => Sync[F].pure(opt)
      }

  override def optimize(items: Seq[A]): F[Optimizer.OptimizationId] =
    for {
      id <- Sync[F].delay(OptimizationId(UUID.randomUUID()))
      _  <- state.update(s => s + (id -> OptimizationResult(id, Instant.now())))
      _  <- Concurrent[F].start(alg.optimizeSeq(items).flatMap(res => updateState(id, res))).void
    } yield id

  private def updateState(id: OptimizationId, result: Seq[A]): F[Unit] =
    for {
      opt <- get(id)
      duration     = FiniteDuration(Instant.now().toEpochMilli - opt.dateInitiated.toEpochMilli, TimeUnit.MILLISECONDS)
      completedOpt = opt.copy(duration = Some(duration), result = Some(result))
      _ <- state.update(s => s + (id -> completedOpt))
    } yield ()
}

object Optimizer {
  final case class OptimizationId(value: UUID) extends AnyVal

  final case class OptimizationResult[A](
      id: OptimizationId,
      dateInitiated: Instant,
      duration: Option[FiniteDuration] = None,
      result: Option[Seq[A]] = None
  )

  def refBasedOptimizer[F[_]: Concurrent: ContextShift, A](
      implicit alg: OptimizationAlgorithm[F, A]
  ): F[Optimizer[F, A]] =
    Ref
      .of[F, Map[OptimizationId, OptimizationResult[A]]](Map())
      .map(s => new RefBasedOptimizer(s))
}

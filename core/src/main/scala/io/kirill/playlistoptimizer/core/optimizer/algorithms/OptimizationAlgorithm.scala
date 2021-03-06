package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.Async
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators._

import scala.reflect.ClassTag
import scala.util.Random

trait OptimizationAlgorithm[F[_], A] {
  def optimize[T](
      target: T,
      parameters: OptimizationParameters,
      updateProgress: BigDecimal => F[Unit]
  )(implicit
      optimizable: Optimizable[T, A],
      rand: Random
  ): F[(T, BigDecimal)]
}

object OptimizationAlgorithm {

  def geneticAlgorithm[F[_]: Async, A: ClassTag](
      crossover: Crossover[A],
      mutator: Mutator[A],
      evaluator: Evaluator[A],
      selector: Selector[A],
      elitism: Elitism[A]
  ): OptimizationAlgorithm[F, A] =
    new GeneticAlgorithm[F, A](crossover, mutator, evaluator, selector, elitism)
}

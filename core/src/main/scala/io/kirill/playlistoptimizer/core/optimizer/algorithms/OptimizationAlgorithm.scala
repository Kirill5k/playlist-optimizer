package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.kernel.Async
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators._

import scala.reflect.ClassTag
import scala.util.Random

trait Optimizable[A] {
  def repr: Array[A]
}

trait OptimizationAlgorithm[F[_], A] {
  def optimize(
      optimizable: Optimizable[A],
      parameters: OptimizationParameters
  )(implicit
      rand: Random
  ): F[(IndexedSeq[A], BigDecimal)]
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

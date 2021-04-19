package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.kernel.Async
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators._

import scala.util.Random

trait OptimizationAlgorithm[F[_], A] {
  def optimizeSeq(
      items: IndexedSeq[A],
      parameters: OptimizationParameters
  )(implicit
      rand: Random
  ): F[(IndexedSeq[A], BigDecimal)]
}

object OptimizationAlgorithm {

  def geneticAlgorithm[F[_]: Async, A](
      crossover: Crossover[A],
      mutator: Mutator[A],
      evaluator: Evaluator[A],
      selector: Selector[A],
      elitism: Elitism[A]
  ): OptimizationAlgorithm[F, A] =
    new GeneticAlgorithm[F, A](crossover, mutator, evaluator, selector, elitism)
}

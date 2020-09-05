package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.Concurrent
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators.{Crossover, Elitism, Evaluator, Mutator, Selector}

import scala.util.Random

trait OptimizationAlgorithm[F[_], A] {
  def optimizeSeq(
      items: IndexedSeq[A],
      parameters: OptimizationParameters
  )(
      implicit rand: Random
  ): F[(IndexedSeq[A], Double)]
}

object OptimizationAlgorithm {

  def geneticAlgorithm[F[_]: Concurrent, A: Crossover: Mutator: Evaluator: Selector: Elitism]: OptimizationAlgorithm[F, A] =
    new GeneticAlgorithm[F, A]()
}

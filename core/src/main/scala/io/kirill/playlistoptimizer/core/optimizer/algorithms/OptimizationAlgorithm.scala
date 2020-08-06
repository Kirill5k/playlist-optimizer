package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.Concurrent
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.operators.{Crossover, Evaluator, Mutator}

import scala.util.Random

trait OptimizationAlgorithm[F[_], A] {
  def optimizeSeq(items: IndexedSeq[A], parameters: OptimizationParameters): F[(IndexedSeq[A], Double)]
}

object OptimizationAlgorithm {

  def geneticAlgorithm[F[_]: Concurrent, A: Crossover: Mutator: Evaluator](
      implicit rand: Random
  ): OptimizationAlgorithm[F, A] =
    new GeneticAlgorithm[F, A]()
}

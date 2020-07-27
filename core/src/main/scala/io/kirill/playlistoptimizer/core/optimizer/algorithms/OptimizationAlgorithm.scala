package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.Concurrent
import io.kirill.playlistoptimizer.core.common.config.GeneticAlgorithmConfig
import io.kirill.playlistoptimizer.core.optimizer.operators.{Crossover, Evaluator, Mutator}

import scala.util.Random

trait OptimizationAlgorithm[F[_], A] {
  def optimizeSeq(items: Seq[A])(implicit r: Random): F[Seq[A]]
}

object OptimizationAlgorithm {

  def geneticAlgorithm[F[_]: Concurrent, A: Crossover: Mutator: Evaluator](config: GeneticAlgorithmConfig) =
    geneticAlgorithm(config.populationSize, config.mutationFactor, config.iterations)

  def geneticAlgorithm[F[_]: Concurrent, A: Crossover: Mutator: Evaluator](
      populationSize: Int,
      mutationFactor: Double,
      iterations: Int
  ): OptimizationAlgorithm[F, A] =
    new GeneticAlgorithm[F, A](populationSize, mutationFactor, iterations)
}

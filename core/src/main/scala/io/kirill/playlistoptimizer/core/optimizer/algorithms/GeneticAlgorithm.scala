package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.Parallel
import cats.effect.{Concurrent, Sync}
import cats.implicits._
import fs2.Stream
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators._

import scala.util.Random

final class GeneticAlgorithm[F[_]: Concurrent: Parallel, A](
    private val crossover: Crossover[A],
    private val mutator: Mutator[A],
    private val evaluator: Evaluator[A],
    private val selector: Selector[A],
    private val elitism: Elitism[A]
) extends OptimizationAlgorithm[F, A] {

  override def optimizeSeq(
      items: IndexedSeq[A],
      params: OptimizationParameters
  )(implicit rand: Random): F[(IndexedSeq[A], BigDecimal)] = {
    val initialPopulation = List.fill(params.populationSize)(if (params.shuffle) rand.shuffle(items) else items)
    Stream
      .range[F](0, params.maxGen)
      .evalScan(initialPopulation)((currPop, _) => singleGeneration(currPop, params))
      .compile
      .lastOrError
      .map(evaluator.evaluatePopulation)
      .map(_.map(p => (p._1, p._2.value)).minBy(_._2))
  }

  private def singleGeneration(
      population: List[IndexedSeq[A]],
      params: OptimizationParameters
  )(implicit rand: Random): F[List[IndexedSeq[A]]] =
    for {
      fitpop <- Sync[F].delay(evaluator.evaluatePopulation(population))
      elites <- Sync[F].delay(elitism.select(fitpop, params.elitismRatio))
      pairs  <- Sync[F].delay(selector.selectPairs(fitpop, params.populationSize))
      prob = params.crossoverProbability
      crossed <- pairs.toList
        .parTraverse { case (p1, p2) =>
          (
            Sync[F].delay(crossover.cross(p1, p2, prob)),
            Sync[F].delay(crossover.cross(p2, p1, prob))
          ).mapN((c1, c2) => List(c1, c2))
        }
        .map(_.flatten)
      mutated <- crossed.parTraverse(i => Sync[F].delay(mutator.mutate(i, params.mutationProbability)))
    } yield mutated ++ elites
}

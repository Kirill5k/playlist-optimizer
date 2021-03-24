package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.{Concurrent, Sync}
import cats.implicits._
import fs2.Stream
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators._

import scala.util.Random

final class GeneticAlgorithm[F[_]: Concurrent, A](
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
  )(implicit rand: Random): F[List[IndexedSeq[A]]] = {
    val fitpop = evaluator.evaluatePopulation(population)
    val elites = Stream.evalSeq(Sync[F].delay(elitism.select(fitpop, params.elitismRatio)))
    val newPopulation = Stream
      .evalSeq(Sync[F].delay(selector.selectPairs(fitpop, params.populationSize)))
      .map { case (p1, p2) =>
        val prob = params.crossoverProbability
        val c1   = Stream.eval(Sync[F].delay(crossover.cross(p1, p2, prob)))
        val c2   = Stream.eval(Sync[F].delay(crossover.cross(p2, p1, prob)))
        c1 ++ c2
      }
      .parJoinUnbounded
      .evalMap(ind => Sync[F].delay(mutator.mutate(ind, params.mutationProbability)))

    (newPopulation ++ elites).compile.toList
  }
}

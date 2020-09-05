package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.{Concurrent, Sync}
import cats.implicits._
import fs2.Stream
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators._

import scala.util.Random

class GeneticAlgorithm[F[_]: Concurrent, A](
    implicit val crossover: Crossover[A],
    val mutator: Mutator[A],
    val evaluator: Evaluator[A],
    val selector: Selector[A],
    val elitism: Elitism[A]
) extends OptimizationAlgorithm[F, A] {

  override def optimizeSeq(
    items: IndexedSeq[A],
    params: OptimizationParameters
  )(
      implicit rand: Random
  ): F[(IndexedSeq[A], Double)] = {
    val initialPopulation = List.fill(params.populationSize)(if (params.shuffle) rand.shuffle(items) else items)
    Stream
      .range[F](0, params.maxGen)
      .evalScan(initialPopulation)((currPop, _) => singleGeneration(currPop, params))
      .compile
      .lastOrError
      .map(finalPop => evaluator.evaluatePopulation(finalPop).map(p => (p._1, p._2.value)).minBy(_._2))
  }

  private def singleGeneration(
      population: List[IndexedSeq[A]],
      params: OptimizationParameters
  )(implicit rand: Random): F[List[IndexedSeq[A]]] = {
    val fitpop = evaluator.evaluatePopulation(population)
    val elites = Stream.evalSeq(Sync[F].delay(elitism.select(fitpop, params.elitismRatio)))
    val newPopulation = Stream
      .evalSeq(Concurrent[F].delay(selector.selectPairs(fitpop, params.populationSize)))
      .map {
        case (p1, p2) =>
          Stream.evalSeq(
            Concurrent[F]
              .delay(List(crossover.cross(p1, p2, params.crossoverProbability), crossover.cross(p2, p1, params.crossoverProbability)))
          )
      }
      .parJoinUnbounded
      .flatMap(ind => Stream.eval(Sync[F].delay(mutator.mutate(ind, params.mutationProbability))))

    (newPopulation ++ elites).compile.toList
  }
}

package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.Async
import cats.implicits._
import fs2.Stream
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators._

import scala.reflect.ClassTag
import scala.util.Random

final class GeneticAlgorithm[F[_], A: ClassTag](
    private val crossover: Crossover[A],
    private val mutator: Mutator[A],
    private val evaluator: Evaluator[A],
    private val selector: Selector[A],
    private val elitism: Elitism[A]
)(implicit
    F: Async[F]
) extends OptimizationAlgorithm[F, A] {

  override def optimize(
      optimizable: Optimizable[A],
      params: OptimizationParameters
  )(implicit rand: Random): F[(Array[A], BigDecimal)] = {
    Stream
      .range[F, Int](0, params.maxGen)
      .evalScan(initializePopulation(optimizable, params))((currPop, _) => singleGeneration(currPop, params))
      .compile
      .lastOrError
      .map(evaluator.evaluatePopulation)
      .map(_.minBy(_._2.value))
      .map { case (res, f) => (res, f.value) }
  }

  private def initializePopulation(
      optimizable: Optimizable[A],
      params: OptimizationParameters
  )(implicit rand: Random): List[Array[A]] = {
    val indGen = () => if (params.shuffle) rand.shuffle(optimizable.repr.toVector).toArray else optimizable.repr
    List.fill(params.populationSize)(indGen())
  }

  private def singleGeneration(
      population: List[Array[A]],
      params: OptimizationParameters
  )(implicit rand: Random): F[List[Array[A]]] =
    for {
      fitpop <- F.delay(evaluator.evaluatePopulation(population))
      elites <- F.delay(elitism.select(fitpop, params.elitismRatio))
      pairs  <- F.delay(selector.selectPairs(fitpop, params.populationSize))
      prob = params.crossoverProbability
      crossed <- F
        .parTraverseN(Int.MaxValue)(pairs.toList) { case (p1, p2) =>
          List(
            F.delay(crossover.cross(p1, p2, prob)),
            F.delay(crossover.cross(p2, p1, prob))
          ).sequence
        }
        .map(_.flatten)
      mutated <- F.parTraverseN(Int.MaxValue)(crossed)(i => F.delay(mutator.mutate(i, params.mutationProbability)))
    } yield mutated ++ elites
}

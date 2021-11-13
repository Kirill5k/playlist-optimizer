package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.Async
import cats.implicits.*
import fs2.Stream
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators.*
import io.kirill.playlistoptimizer.core.optimizer.algorithms.Optimizable.*

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

  override def optimize[T](
      target: T,
      params: OptimizationParameters,
      updateProgress: BigDecimal => F[Unit]
  )(using
      optimizable: Optimizable[T, A],
      rand: Random
  ): F[(T, BigDecimal)] =
    Stream
      .range[F, Int](0, params.maxGen)
      .map(i => i.toDouble * 100 / params.maxGen)
      .evalScan(initializePopulation(target.repr, params)) { (currPop, i) =>
        (if (i * 10 % 10 == 0) updateProgress(i) else F.unit) *> singleGeneration(currPop, params)
      }
      .compile
      .lastOrError
      .map(evaluator.evaluatePopulation)
      .map(_.minBy(_._2.value))
      .map { case (res, f) => (optimizable.update(target)(res), f.value) }

  private def initializePopulation(
      repr: Array[A],
      params: OptimizationParameters
  )(using rand: Random): Vector[Array[A]] =
    Vector.fill(params.populationSize)(if (params.shuffle) rand.shuffle(repr.toVector).toArray else repr)

  private def singleGeneration(
      population: Vector[Array[A]],
      params: OptimizationParameters
  )(using rand: Random): F[Vector[Array[A]]] =
    for {
      fitpop <- F.delay(evaluator.evaluatePopulation(population))
      elites <- F.delay(elitism.select(fitpop, params.elitismRatio))
      pairs  <- F.delay(selector.selectPairs(fitpop, params.populationSize))
      crossed <- F
        .parTraverseN(Int.MaxValue)(pairs.toVector) { case (p1, p2) =>
          List(
            F.delay(crossover.cross(p1, p2, params.crossoverProbability)),
            F.delay(crossover.cross(p2, p1, params.crossoverProbability))
          ).sequence
        }
        .map(_.flatten)
      mutated <- F.parTraverseN(Int.MaxValue)(crossed)(i => F.delay(mutator.mutate(i, params.mutationProbability)))
    } yield mutated ++ elites
}

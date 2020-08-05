package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.Concurrent
import cats.implicits._
import fs2.Stream
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.operators.{Crossover, Evaluator, Mutator}
import io.kirill.playlistoptimizer.core.utils.CollectionOps._

import scala.util.Random

class GeneticAlgorithm[F[_]: Concurrent, A: Crossover: Mutator: Evaluator](
    implicit val rand: Random
) extends OptimizationAlgorithm[F, A] {

  override def optimizeSeq(items: Seq[A], params: OptimizationParameters): F[(Seq[A], Double)] = {
    val initialPopulation =
      if (params.shuffle) items.shuffledCopies(params.populationSize)
      else List.fill(params.populationSize)(items)
    Stream
      .range[F](0, params.iterations)
      .evalScan(initialPopulation)((currPop, _) => singleIteration(currPop, params.mutationFactor))
      .compile
      .lastOrError
      .map(_.head)
      .map(res => (res, Evaluator[A].evaluate(res)))
  }

  private def singleIteration(population: Seq[Seq[A]], mutationFactor: Double)(implicit e: Evaluator[A]): F[Seq[Seq[A]]] = {
    val newPopulation = Stream
      .evalSeq(Concurrent[F].delay(population.pairs))
      .map { case (p1, p2) =>
        Stream.evalSeq(Concurrent[F].delay(List(breed(p1, p2, mutationFactor), breed(p2, p1, mutationFactor))))
      }
      .parJoinUnbounded

    val oldPopulation = Stream.evalSeq(population.pure[F])

    (newPopulation ++ oldPopulation).compile.toList.map(_.sortBy(e.evaluate).take(population.size))
  }

  private def breed(p1: Seq[A], p2: Seq[A], mutationFactor: Double)(implicit c: Crossover[A], m: Mutator[A], r: Random): Seq[A] = {
    val child = c.cross(p1, p2)
    if (r.nextDouble < mutationFactor) m.mutate(child) else child
  }
}

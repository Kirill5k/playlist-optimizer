package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.Concurrent
import cats.implicits._
import fs2.Stream
import io.kirill.playlistoptimizer.core.optimizer.operators.{Crossover, Evaluator, Mutator}
import io.kirill.playlistoptimizer.core.utils.CollectionOps._

import scala.util.Random

class GeneticAlgorithm[F[_]: Concurrent, A: Crossover: Mutator: Evaluator](
    val populationSize: Int,
    val mutationFactor: Double,
    val iterations: Int
)(
    implicit val rand: Random
) extends OptimizationAlgorithm[F, A] {

  override def optimizeSeq(items: Seq[A]): F[Seq[A]] =
    Stream
      .range[F](0, iterations)
      .evalScan(items.shuffledCopies(populationSize))((currPop, _) => singleIteration(currPop))
      .compile
      .lastOrError
      .map(_.head)

  private def singleIteration(population: Seq[Seq[A]])(implicit e: Evaluator[A]): F[Seq[Seq[A]]] = {
    val newPopulation = Stream
      .evalSeq(Concurrent[F].delay(population.pairs))
      .map { case (p1, p2) => Stream.evalSeq(Concurrent[F].delay(List(breed(p1, p2), breed(p2, p1)))) }
      .parJoinUnbounded

    val oldPopulation = Stream.evalSeq(Concurrent[F].pure(population))

    (newPopulation ++ oldPopulation).compile.toList.map(_.sortBy(e.evaluate).take(populationSize))
  }

  private def breed(p1: Seq[A], p2: Seq[A])(implicit c: Crossover[A], m: Mutator[A], r: Random): Seq[A] = {
    val child = c.cross(p1, p2)
    if (r.nextDouble < mutationFactor) m.mutate(child) else child
  }
}

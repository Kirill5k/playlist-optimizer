package io.kirill.playlistoptimizer.core.optimizer

import cats.effect._
import cats.implicits._
import fs2.Stream
import io.kirill.playlistoptimizer.core.common.configs.GeneticAlgorithmConfig
import io.kirill.playlistoptimizer.core.optimizer.operators.{Crossover, Evaluator, Mutator}
import io.kirill.playlistoptimizer.optimizer.operators.Crossover
import io.kirill.playlistoptimizer.core.utils.CollectionOps._

import scala.util.Random

trait Optimizer[F[_], A] {
  def optimize(items: Seq[A])(implicit e: Evaluator[A], r: Random): F[Seq[A]]
}

object Optimizer {
  implicit def geneticAlgorithmOptimizer[F[_]: Concurrent, A: Crossover : Mutator](config: GeneticAlgorithmConfig): Optimizer[F, A] =
    new Optimizer[F, A] {
      val popSize = config.populationSize
      val mutFactor = config.mutationFactor
      val its = config.iterations

      override def optimize(items: Seq[A])(implicit e: Evaluator[A], r: Random): F[Seq[A]] =
        Stream.range[F](0, its)
          .evalScan(items.shuffledCopies(popSize))((currPop, _) => singleIteration(currPop))
          .compile
          .lastOrError
          .map(_.head)

      private def singleIteration(population: Seq[Seq[A]])(implicit e: Evaluator[A], r: Random): F[Seq[Seq[A]]] = {
        val newPopulation = Stream.evalSeq(Concurrent[F].delay(population.pairs))
          .map { case (p1, p2) => Stream.evalSeq(Concurrent[F].delay(List(breed(p1, p2), breed(p2, p1)))) }
          .parJoinUnbounded

        val oldPopulation = Stream.evalSeq(Concurrent[F].pure(population))

        (newPopulation ++ oldPopulation).compile.toList.map(_.sortBy(e.evaluate).take(popSize))
      }

      private def breed(p1: Seq[A], p2: Seq[A])(implicit c: Crossover[A], m: Mutator[A], r: Random): Seq[A] = {
        val child = c.cross(p1, p2)
        if (r.nextDouble < mutFactor) m.mutate(child) else child
      }
  }
}

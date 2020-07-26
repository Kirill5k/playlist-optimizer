package io.kirill.playlistoptimizer.core.optimizer

import cats.effect._
import cats.implicits._
import cats.effect.Concurrent
import fs2.Stream
import io.kirill.playlistoptimizer.core.common.config.GeneticAlgorithmConfig
import io.kirill.playlistoptimizer.core.optimizer.operators.{Crossover, Evaluator, Mutator}

import scala.util.Random

import io.kirill.playlistoptimizer.core.utils.CollectionOps._

object algorithms {

  trait Algorithm[F[_], A] {
    def min(items: Seq[A]): F[Seq[A]]
  }

  def geneticAlgorithm[F[_]: Concurrent, A: Crossover : Mutator: Evaluator](implicit config: GeneticAlgorithmConfig, r: Random): Algorithm[F, A] =
    new Algorithm[F, A] {
      val popSize = config.populationSize
      val mutFactor = config.mutationFactor
      val its = config.iterations

      override def min(items: Seq[A]): F[Seq[A]] =
        Stream.range[F](0, its)
          .evalScan(items.shuffledCopies(popSize))((currPop, _) => singleIteration(currPop))
          .compile
          .lastOrError
          .map(_.head)

      private def singleIteration(population: Seq[Seq[A]]): F[Seq[Seq[A]]] = {
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

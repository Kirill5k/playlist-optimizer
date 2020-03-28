package io.kirill.playlistoptimizer.optimizer

import cats.effect._
import cats.implicits._
import fs2.Stream
import io.kirill.playlistoptimizer.common.configs.GeneticAlgorithmConfig
import io.kirill.playlistoptimizer.optimizer.operators.{Crossover, Evaluator, Mutator}
import io.kirill.playlistoptimizer.utils.CollectionOps._

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

    private def singleIteration(population: Seq[Seq[A]])(implicit e: Evaluator[A], c: Crossover[A], m: Mutator[A], r: Random): F[Seq[Seq[A]]] = {
      val newPopulation = Stream.evalSeq(Concurrent[F].delay(population.pairs))
        .parEvalMap(8) { case (p1, p2) => Concurrent[F].delay(List(c.cross(p1, p2), c.cross(p2, p1))) }
        .flatMap(pairs => Stream.evalSeq(Concurrent[F].pure(pairs)))
        .parEvalMap(8)(ind => if (r.nextDouble < mutFactor) Concurrent[F].delay(m.mutate(ind)) else Concurrent[F].pure(ind))

      val oldPopulation = Stream.evalSeq(Concurrent[F].pure(population))

      (newPopulation ++ oldPopulation).compile.toList.map(_.sortBy(e.evaluate).take(popSize))
    }
  }
}

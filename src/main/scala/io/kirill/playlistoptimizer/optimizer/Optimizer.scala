package io.kirill.playlistoptimizer.optimizer

import cats.effect._
import io.kirill.playlistoptimizer.optimizer.operators.{Crossover, Evaluator, Mutator}
import io.kirill.playlistoptimizer.utils.CollectionOps._

import scala.util.Random

trait Optimizer[F[_], A] {
  def optimize(items: Seq[A])(implicit e: Evaluator[A], r: Random): F[Seq[A]]
}

object Optimizer {
  implicit def geneticAlgorithmOptimizer[F[_]: Sync, A: Crossover : Mutator](populationSize: Int, iterations: Int, mutationFactor: Double): Optimizer[F, A] =
    new Optimizer[F, A] {
      override def optimize(items: Seq[A])(implicit e: Evaluator[A], r: Random): F[Seq[A]] = Sync[F].delay {
        (0 until iterations)
          .foldLeft(items.shuffledCopies(populationSize))((currentPopulation, _) => singleIteration(currentPopulation))
          .head
    }

    private def singleIteration(population: Seq[Seq[A]])(implicit e: Evaluator[A], c: Crossover[A], m: Mutator[A], r: Random): Seq[Seq[A]] = {
      val newPopulation = population.pairs
        .flatMap { case (p1, p2) => List(c.cross(p1, p2), c.cross(p2, p1)) }
        .map(i => if (r.nextDouble < mutationFactor) m.mutate(i) else i)

      (newPopulation ++ population).sortBy(e.evaluate).take(populationSize)
    }
  }
}

package io.kirill.playlistoptimizer.optimizer

import cats.effect._
import io.kirill.playlistoptimizer.optimizer.operators.{Crossover, Evaluator, Mutator}
import io.kirill.playlistoptimizer.utils.CollectionOps._

import scala.util.Random

trait Optimizer[F[_], A] {
  def optimize(items: Seq[A])(implicit evaluator: Evaluator[A], R: Random): F[Seq[A]]
}

object Optimizer {
  implicit def geneticAlgorithmOptimizer[F[_]: Sync, A: Crossover : Mutator](populationSize: Int, iterations: Int, mutationFactor: Double): Optimizer[F, A] =
    new Optimizer[F, A] {
      override def optimize(items: Seq[A])(implicit E: Evaluator[A], R: Random): F[Seq[A]] = Sync[F].delay {
        (0 until iterations)
          .foldLeft(items.shuffledCopies(populationSize))((currentPopulation, _) => singleIteration(currentPopulation))
          .head
    }

    private def singleIteration(population: Seq[Seq[A]])(implicit E: Evaluator[A], C: Crossover[A], M: Mutator[A], R: Random): Seq[Seq[A]] = {
      val newPopulation = population.pairs
        .flatMap { case (p1, p2) => List(C.cross(p1, p2), C.cross(p2, p1)) }
        .map(m => if (R.nextDouble < mutationFactor) M.mutate(m) else m)

      (newPopulation ++ population).sortBy(E.evaluate).take(populationSize)
    }
  }
}

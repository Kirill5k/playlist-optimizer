package io.kirill.playlistoptimizer.optimizer

import io.kirill.playlistoptimizer.utils.CollectionOps._

import scala.util.Random

trait Optimizer[A] {
  def optimize(items: Seq[A])(implicit evaluator: Evaluator[A], R: Random): Seq[A]
}

object Optimizer {
  implicit def geneticAlgorithmOptimizer[A](populationSize: Int, iterations: Int, mutationFactor: Double): Optimizer[A] = new Optimizer[A] {
    override def optimize(items: Seq[A])(implicit E: Evaluator[A], R: Random): Seq[A] = {
      (0 until iterations)
        .foldLeft(initPopulation(items, populationSize))((currentPopulation, _) => singleIteration(currentPopulation))
        .head
    }

    private def singleIteration(population: Seq[Seq[A]])(implicit E: Evaluator[A], C: Crossover[A], M: Mutator[A], R: Random): Seq[Seq[A]] = {
      val newPopulation = distributeInPairs(population)
        .flatMap { case (p1, p2) => IndexedSeq(C.cross(p1, p2), C.cross(p2, p1)) }
        .map(m => if (R.nextDouble < mutationFactor) M.mutate(m) else m)

      (newPopulation ++ population).sortBy(E.evaluate).take(populationSize)
    }
  }

  private[optimizer] def initPopulation[A](initialSolution: Seq[A], size: Int): Seq[Seq[A]] =
    List.fill(size)(Random.shuffle(initialSolution))

  private[optimizer] def distributeInPairs[A](population: Seq[A]): Seq[(A, A)] = {
    val half = population.removeNth(2)
    half.zip(population.filterNot(half.contains))
  }
}

package io.kirill.playlistoptimizer.optimizer

import io.kirill.playlistoptimizer.utils.CollectionOps._

import scala.util.Random

trait Optimizer[A] {
  def optimize(items: IndexedSeq[A])(implicit evaluator: Evaluator[A], R: Random): IndexedSeq[A]
}

object Optimizer {
  implicit def geneticAlgorithmOptimizer[A](populationSize: Int, iterations: Int, mutationFactor: Double): Optimizer[A] = new Optimizer[A] {
    override def optimize(items: IndexedSeq[A])(implicit E: Evaluator[A], R: Random): IndexedSeq[A] = {
      (0 until iterations)
        .foldLeft(initPopulation(items, populationSize))((currentPopulation, _) => singleIteration(currentPopulation))
        .head
    }

    private def singleIteration(population: Seq[IndexedSeq[A]])(implicit E: Evaluator[A], R: Random): Seq[IndexedSeq[A]] = {
      val newPopulation = distributeInPairs(population)
        .flatMap { case (p1, p2) => IndexedSeq(crossover(p1, p2), crossover(p2, p1)) }
        .map(m => if (R.nextDouble < mutationFactor) Optimizer.mutate(m) else m)

      (newPopulation ++ population).sortBy(E.evaluate).take(populationSize)
    }
  }

  private[optimizer] def initPopulation[A](initialSolution: IndexedSeq[A], size: Int): Seq[IndexedSeq[A]] =
    List.fill(size)(Random.shuffle(initialSolution))

  private[optimizer] def distributeInPairs[A](population: Seq[A]): Seq[(A, A)] = {
    val half = population.removeNth(2)
    half.zip(population.filterNot(half.contains))
  }

  private[optimizer] def mutate[A](is: IndexedSeq[A])(implicit R: Random): IndexedSeq[A] =
    is.swap(R.nextInt(is.size), R.nextInt(is.size))

  private[optimizer] def crossover[A](is1: IndexedSeq[A], is2: IndexedSeq[A])(implicit R: Random): IndexedSeq[A] = {
    val middle = is1.size / 2
    val point1: Int = R.nextInt(middle)
    val point2: Int = R.nextInt(middle) + middle
    val (l, m, r) = is1.splitInThree(point1, point2)
    l ++ is2.filter(m.contains) ++ r
  }

}

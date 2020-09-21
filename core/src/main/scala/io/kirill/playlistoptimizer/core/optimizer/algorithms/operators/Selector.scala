package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators.operators.Fitness
import io.kirill.playlistoptimizer.core.utils.CollectionOps._

import scala.annotation.tailrec
import scala.util.Random

trait Selector[A] {
  def selectPairs(
      population: Seq[(IndexedSeq[A], Fitness)],
      populationLimit: Int
  )(implicit r: Random): Seq[(IndexedSeq[A], IndexedSeq[A])]
}

final class FitnessBasedSelector[A] extends Selector[A] {

  override def selectPairs(
      population: Seq[(IndexedSeq[A], Fitness)],
      populationLimit: Int
  )(
      implicit r: Random
  ): Seq[(IndexedSeq[A], IndexedSeq[A])] =
    population
      .sortBy(_._2.value)
      .map(_._1)
      .take(populationLimit)
      .pairs
}

final class RouletteWheelSelector[A] extends Selector[A] {

  override def selectPairs(
      population: Seq[(IndexedSeq[A], Fitness)],
      populationLimit: Int
  )(
      implicit r: Random
  ): Seq[(IndexedSeq[A], IndexedSeq[A])] = {
    val popByFitness = population
      .toList
      .sortBy(_._2.value)
      .map {
        case (i, f) => (i, 100 / f.value)
      }

    @tailrec
    def go(newPop: List[IndexedSeq[A]], remPop: Seq[(IndexedSeq[A], BigDecimal)]): List[IndexedSeq[A]] =
      if (remPop.isEmpty || newPop.size >= populationLimit) newPop
      else {
        val (pickedInd, remaining) = pickOne(remPop)
        go(pickedInd :: newPop, remaining)
      }
    go(List(), popByFitness).reverse.pairs
  }

  private def pickOne(
      popByFitness: Seq[(IndexedSeq[A], BigDecimal)]
  )(
      implicit r: Random
  ): (IndexedSeq[A], Seq[(IndexedSeq[A], BigDecimal)]) = {
    val fTotal = popByFitness.map(_._2).sum

    val popByCumulativeSum = popByFitness
      .map {
        case (i, f) => (i, f / fTotal)
      }
      .tails
      .take(popByFitness.size)
      .map(t => (t.head._1, t.map(_._2).sum))
      .toList
      .reverse

    val n = r.nextDouble()

    val i = popByCumulativeSum.indexWhere(_._2 >= n, 0)

    val ind    = popByCumulativeSum(i)._1
    val remPop = popByFitness.take(popByFitness.size - i - 1) ++ popByFitness.drop(popByFitness.size - i)
    (ind, remPop)
  }
}

object Selector {
  def rouletteWheelSelector[A]: Selector[A] = new RouletteWheelSelector[A]

  def fitnessBasedSelector[A]: Selector[A] = new FitnessBasedSelector[A]
}

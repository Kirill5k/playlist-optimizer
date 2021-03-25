package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.utils.collections._

import scala.annotation.tailrec
import scala.util.Random

trait Selector[A] {
  def selectPairs(
      population: Seq[(IndexedSeq[A], Fitness)],
      populationLimit: Int
  )(implicit r: Random): Seq[(IndexedSeq[A], IndexedSeq[A])]
}

final private class FitnessBasedSelector[A] extends Selector[A] {

  override def selectPairs(
      population: Seq[(IndexedSeq[A], Fitness)],
      populationLimit: Int
  )(implicit r: Random): Seq[(IndexedSeq[A], IndexedSeq[A])] =
    population
      .sortBy(_._2.value)
      .map(_._1)
      .take(populationLimit)
      .pairs
}

final private class RouletteWheelSelector[A] extends Selector[A] {

  override def selectPairs(
      population: Seq[(IndexedSeq[A], Fitness)],
      populationLimit: Int
  )(implicit r: Random): Seq[(IndexedSeq[A], IndexedSeq[A])] = {
    val popByFitness = population
      .sortBy(_._2.value)
      .map { case (i, f) => (i, 100 / f.value) }

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
  )(implicit r: Random): (IndexedSeq[A], Seq[(IndexedSeq[A], BigDecimal)]) = {
    val fTotal = popByFitness.map(_._2).sum

    var remFitness = BigDecimal(1.0)

    val n = r.nextDouble()
    val i = LazyList
      .from(popByFitness)
      .map { case (i, f) =>
        val res = (i, remFitness)
        remFitness -= f / fTotal
        res
      }
      .indexWhere(_._2 < n, 0) - 1

    if (i >= 0) {
      val ind    = popByFitness(i)._1
      val remPop = popByFitness.take(i) ++ popByFitness.drop(i + 1)
      (ind, remPop)
    } else {
      (popByFitness.head._1, popByFitness.tail)
    }
  }
}

object Selector {
  def rouletteWheelSelector[A]: Selector[A] = new RouletteWheelSelector[A]

  def fitnessBasedSelector[A]: Selector[A] = new FitnessBasedSelector[A]
}

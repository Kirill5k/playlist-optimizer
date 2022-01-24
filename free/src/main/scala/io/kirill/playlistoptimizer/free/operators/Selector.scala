package io.kirill.playlistoptimizer.free.operators

import io.kirill.playlistoptimizer.free.{Ind, Fitness}
import io.kirill.playlistoptimizer.domain.utils.collections.*
import scala.annotation.tailrec
import scala.util.Random

trait Selector[A]:
  def selectPairs(population: Seq[(Ind[A], Fitness)], populationLimit: Int)(using r: Random): Seq[(Ind[A], Ind[A])]

final private class FitnessBasedSelector[A] extends Selector[A] {
  override def selectPairs(
      population: Seq[(Ind[A], Fitness)],
      populationLimit: Int
  )(using r: Random): Seq[(Ind[A], Ind[A])] =
    population
      .sortBy(_._2)
      .take(populationLimit)
      .map(_._1)
      .pairs
}

final private class RouletteWheelSelector[A] extends Selector[A] {
  override def selectPairs(
      population: Seq[(Ind[A], Fitness)],
      populationLimit: Int
  )(using r: Random): Seq[(Ind[A], Ind[A])] = {
    val popByFitness = population
      .sortBy(_._2)
      .map { case (i, f) => (i, 100 / f.value) }

    val fTotal = popByFitness.map(_._2).sum

    @tailrec
    def go(newPop: List[Ind[A]], remPop: Seq[(Ind[A], BigDecimal)], f: BigDecimal): List[Ind[A]] =
      if (remPop.isEmpty || newPop.size >= populationLimit) newPop
      else {
        val ((pickedInd, indFitness), remaining) = pickOne(remPop, f)
        go(pickedInd :: newPop, remaining, f - indFitness)
      }
    go(Nil, popByFitness, fTotal).reverse.pairs
  }

  private def pickOne(
      popByFitness: Seq[(Ind[A], BigDecimal)],
      fTotal: BigDecimal
  )(using r: Random): ((Ind[A], BigDecimal), Seq[(Ind[A], BigDecimal)]) = {
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
      val ind    = popByFitness(i)
      val remPop = popByFitness.take(i) ++ popByFitness.drop(i + 1)
      (ind, remPop)
    } else {
      (popByFitness.head, popByFitness.tail)
    }
  }
}

object Selector:
  inline def rouletteWheelSelector[A]: Selector[A] = new RouletteWheelSelector[A]
  inline def fitnessBasedSelector[A]: Selector[A]  = new FitnessBasedSelector[A]

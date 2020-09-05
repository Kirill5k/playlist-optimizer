package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.common.errors.CalculationError
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

final class RouletteWheelSelector[A] extends Selector[A] {

  override def selectPairs(
      population: Seq[(IndexedSeq[A], Fitness)],
      populationLimit: Int
  )(
      implicit r: Random
  ): Seq[(IndexedSeq[A], IndexedSeq[A])] = {
    @tailrec
    def go(newPop: Seq[IndexedSeq[A]], remPop: Seq[(IndexedSeq[A], Fitness)]): Seq[IndexedSeq[A]] =
      if (remPop.isEmpty || newPop.size >= populationLimit) newPop
      else {
        val pickedInd = pickOne(remPop)
        go(newPop :+ pickedInd, remPop.filter(_._1 != pickedInd))
      }
    go(List(), population).pairs
  }

  private def pickOne(population: Seq[(IndexedSeq[A], Fitness)])(implicit r: Random): IndexedSeq[A] = {
    val popByFitness = population
      .sortBy(_._2.value)
      .map {
        case (i, f) => (i, 100 / f.value)
      }

    val fTotal = popByFitness.map(_._2).sum

    val popByCumulativeSum = popByFitness
      .map {
        case (i, f) => (i, f / fTotal)
      }
      .tails
      .take(population.size)
      .map(t => (t.head._1, t.map(_._2).sum))
      .toVector
      .reverse

    val n = r.nextDouble()

    popByCumulativeSum
      .find {
        case (_, p) => p >= n
      }
      .map(_._1)
      .getOrElse(throw CalculationError("error getting individual via roulette wheel selector"))
  }
}

object Selector {
  def rouletteWheelSelector[A]: Selector[A] = new RouletteWheelSelector[A]
}

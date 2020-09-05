package io.kirill.playlistoptimizer.core.optimizer.operators

import io.kirill.playlistoptimizer.core.common.errors.CalculationError

import scala.annotation.tailrec
import scala.util.Random

trait Selector {
  def select[A](population: IndexedSeq[(A, Double)])(implicit r: Random): IndexedSeq[A]
}

final class RouletteWheelSelector extends Selector {

  override def select[A](population: IndexedSeq[(A, Double)])(implicit r: Random): IndexedSeq[A] = {
    @tailrec
    def go(newPop: IndexedSeq[A], remPop: IndexedSeq[(A, Double)]): IndexedSeq[A] = {
      if (remPop.isEmpty) newPop
      else {
        val pickedInd = pickOne(remPop)
        go(newPop :+ pickedInd, remPop.filter(_._1 != pickedInd))
      }
    }
    go(Vector(), population)
  }

  private def pickOne[A](population: IndexedSeq[(A, Double)])(implicit r: Random): A = {
    val popByFitness = population
      .sortBy(_._2)
      .map {
        case (i, f) => (i, 100 / f)
      }

    val fTotal = popByFitness.map(_._2).sum

    val popByCumulativeSum = popByFitness
      .map {
        case (i, f) => (i, f / fTotal)
      }.tails
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
  def rouletteWheelSelector: Selector = new RouletteWheelSelector
}

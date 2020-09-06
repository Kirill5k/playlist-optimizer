package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators.operators.Fitness
import io.kirill.playlistoptimizer.core.playlist.{Key, Track}
import io.kirill.playlistoptimizer.core.playlist.Key

sealed trait Evaluator[A] {
  def evaluateIndividual(individual: IndexedSeq[A]): Fitness

  def evaluatePopulation(population: Seq[IndexedSeq[A]]): Seq[(IndexedSeq[A], Fitness)] =
    population.map(ind => (ind, evaluateIndividual(ind)))
}

object Evaluator {
  implicit def keyDistanceBasedTracksEvaluator: Evaluator[Track] = new Evaluator[Track] {
    override def evaluateIndividual(tracks: IndexedSeq[Track]): Fitness = {
      val score = calcScore[Track](tracks, (prev, curr) => math.pow(Key.distance(prev.audio.key, curr.audio.key), 2))
      Fitness(BigDecimal(score).setScale(4, BigDecimal.RoundingMode.HALF_UP))
    }
  }

  def energyFlowBasedTracksEvaluator: Evaluator[Track] = new Evaluator[Track] {
    override def evaluateIndividual(individual: IndexedSeq[Track]): Fitness = {
      val score = calcScore[Track](individual, (prev, curr) => math.abs(prev.audio.energy - curr.audio.energy))
      Fitness(BigDecimal(score).setScale(4, BigDecimal.RoundingMode.HALF_UP))
    }
  }

  def danceabilityBasedTracksEvaluator: Evaluator[Track] = new Evaluator[Track] {
    override def evaluateIndividual(individual: IndexedSeq[Track]): Fitness = {
      val score = calcScore[Track](individual, (prev, curr) => math.abs(prev.audio.danceability - curr.audio.danceability))
      Fitness(BigDecimal(score).setScale(4, BigDecimal.RoundingMode.HALF_UP))
    }
  }

  private def calcScore[A](individual: IndexedSeq[A], calculation: (A, A) => Double): Double =
    individual.tail.foldLeft[(Double, A)]((0, individual.head)) {
      case ((acc, prev), curr) => (acc + calculation(prev, curr), curr)
    }._1
}

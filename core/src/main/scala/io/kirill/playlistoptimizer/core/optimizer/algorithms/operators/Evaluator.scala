package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.playlist.{Key, Track}

final case class Fitness(value: BigDecimal) extends AnyVal

trait Evaluator[A] {
  def evaluateIndividual(individual: IndexedSeq[A]): Fitness

  def evaluatePopulation(population: Seq[IndexedSeq[A]]): Seq[(IndexedSeq[A], Fitness)] =
    population.map(ind => (ind, evaluateIndividual(ind)))

  private def calcFitness(individual: IndexedSeq[A])(calculation: (A, A) => Double): Fitness = {
    val score = individual.toList.tail
      .foldLeft((0d, individual.head)) { case ((acc, prev), curr) =>
        (acc + calculation(prev, curr), curr)
      }
      ._1
    Fitness(BigDecimal(score).setScale(0, BigDecimal.RoundingMode.HALF_UP))
  }
}

object Evaluator {
  implicit def harmonicSeqBasedTracksEvaluator: Evaluator[Track] = new Evaluator[Track] {
    override def evaluateIndividual(tracks: IndexedSeq[Track]): Fitness =
      calcFitness(tracks)((prev, curr) => math.pow(Key.distance(prev.audio.key, curr.audio.key).toDouble, 2.0))
  }

  def energyFlowBasedTracksEvaluator: Evaluator[Track] = new Evaluator[Track] {
    override def evaluateIndividual(individual: IndexedSeq[Track]): Fitness =
      calcFitness(individual) { (prev, curr) =>
        val energy       = math.abs(prev.audio.energy * 1000 - curr.audio.energy * 1000)
        val danceability = math.abs(prev.audio.danceability * 1000 - curr.audio.danceability * 1000)
        (energy + danceability) / 10
      }
  }
}

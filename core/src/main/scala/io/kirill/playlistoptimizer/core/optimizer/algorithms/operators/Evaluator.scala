package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.playlist.{Key, Track}

final case class Fitness(value: BigDecimal) extends AnyVal

sealed trait FitnessCalculation[A]:
  def evaluate(gene1: A, gene2: A): Double

object HarmonicSeqBasedTracksFitnessCalculation extends FitnessCalculation[Track] {
  override def evaluate(gene1: Track, gene2: Track): Double =
    math.pow(Key.distance(gene1.audio.key, gene2.audio.key).toDouble, 2.0)
}

object EnergyFlowBasedTracksFitnessCalculation extends FitnessCalculation[Track] {
  override def evaluate(gene1: Track, gene2: Track): Double = {
    val energy       = math.abs(gene1.audio.energy * 1000 - gene2.audio.energy * 1000)
    val danceability = math.abs(gene1.audio.danceability * 1000 - gene2.audio.danceability * 1000)
    (energy + danceability) / 10
  }
}

trait Evaluator[A] {
  def evaluateIndividual(individual: Array[A]): Fitness

  def evaluatePopulation(population: Seq[Array[A]]): Seq[(Array[A], Fitness)] =
    population.map(ind => (ind, evaluateIndividual(ind)))

  protected def calcFitness(individual: Array[A])(calculation: FitnessCalculation[A]): Fitness = {
    var i = 0
    var score = 0d
    while (i < individual.length - 1) {
      score += calculation.evaluate(individual(i), individual(i+1))
      i += 1
    }
    Fitness(BigDecimal(score).setScale(0, BigDecimal.RoundingMode.HALF_UP))
  }
}

object Evaluator {
  val harmonicSeqBasedTracksEvaluator: Evaluator[Track] = new Evaluator[Track] {
    override def evaluateIndividual(tracks: Array[Track]): Fitness =
      calcFitness(tracks)(HarmonicSeqBasedTracksFitnessCalculation)
  }

  val energyFlowBasedTracksEvaluator: Evaluator[Track] = new Evaluator[Track] {
    override def evaluateIndividual(individual: Array[Track]): Fitness =
      calcFitness(individual)(EnergyFlowBasedTracksFitnessCalculation)
  }
}

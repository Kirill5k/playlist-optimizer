package io.kirill.playlistoptimizer.free.operators

import io.kirill.playlistoptimizer.domain.playlist.{Key, Track}
import io.kirill.playlistoptimizer.free.{Fitness, Ind}

private trait FitnessCalculation[A]:
  def evaluate(gene1: A, gene2: A): Double

private object HarmonicSeqBasedTracksFitnessCalculation extends FitnessCalculation[Track]:
  override def evaluate(gene1: Track, gene2: Track): Double =
    math.pow(Key.distance(gene1.audio.key, gene2.audio.key).toDouble, 2.0)

private object EnergyFlowBasedTracksFitnessCalculation extends FitnessCalculation[Track]:
  override def evaluate(gene1: Track, gene2: Track): Double = {
    val energy       = math.abs(gene1.audio.energy * 1000 - gene2.audio.energy * 1000)
    val danceability = math.abs(gene1.audio.danceability * 1000 - gene2.audio.danceability * 1000)
    (energy + danceability) / 10
  }

trait Evaluator[A] {
  def evaluateIndividual(individual: Ind[A]): (Ind[A], Fitness)

  protected inline def calcFitness(individual: Ind[A])(calculation: FitnessCalculation[A]): (Ind[A], Fitness) = {
    var i     = 0
    var score = 0d
    while (i < individual.length - 1) {
      score += calculation.evaluate(individual(i), individual(i + 1))
      i += 1
    }
    (individual, Fitness(BigDecimal(score).setScale(0, BigDecimal.RoundingMode.HALF_UP)))
  }
}

object Evaluator:
  inline def harmonicSeqBasedTracksEvaluator: Evaluator[Track] = new Evaluator[Track] {
    override def evaluateIndividual(tracks: Ind[Track]): (Ind[Track], Fitness) =
      calcFitness(tracks)(HarmonicSeqBasedTracksFitnessCalculation)
  }

  inline def energyFlowBasedTracksEvaluator: Evaluator[Track] = new Evaluator[Track] {
    override def evaluateIndividual(individual: Ind[Track]): (Ind[Track], Fitness) =
      calcFitness(individual)(EnergyFlowBasedTracksFitnessCalculation)
  }

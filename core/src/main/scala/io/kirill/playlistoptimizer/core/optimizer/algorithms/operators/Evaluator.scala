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
  implicit val keyDistanceBasedTracksEvaluator: Evaluator[Track] = new Evaluator[Track] {
    override def evaluateIndividual(tracks: IndexedSeq[Track]): Fitness = {
      val score = tracks.sliding(2).foldLeft[Double](0) {
        case (acc, t1 +: t2 +: _) => math.pow(Key.distance(t1.audio.key, t2.audio.key), 2) + acc
      }
      Fitness(score)
    }
  }

  @scala.inline
  def apply[A](implicit instance : Evaluator[A]): Evaluator[A] = instance
}

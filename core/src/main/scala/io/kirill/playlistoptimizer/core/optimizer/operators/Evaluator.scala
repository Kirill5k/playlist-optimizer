package io.kirill.playlistoptimizer.core.optimizer.operators

import io.kirill.playlistoptimizer.core.playlist.{Key, Track}
import io.kirill.playlistoptimizer.playlist.Key

sealed trait Evaluator[A] {
  def evaluate(items: Seq[A]): Double
}

object Evaluator {
  implicit val keyDistanceBasedTracksEvaluator: Evaluator[Track] = new Evaluator[Track] {
    override def evaluate(tracks: Seq[Track]): Double =
      tracks.sliding(2).foldLeft[Double](0) {
        case (acc, t1 +: t2 +: _) => math.pow(Key.distance(t1.audio.key, t2.audio.key), 2) + acc
      }
  }
}

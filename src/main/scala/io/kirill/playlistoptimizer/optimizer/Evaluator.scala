package io.kirill.playlistoptimizer.optimizer

import io.kirill.playlistoptimizer.domain.{Key, Track}

sealed trait Evaluator[A] {
  def evaluate(items: Seq[A]): Double
}

object Evaluator {
  implicit val tracksEvaluator: Evaluator[Track] = new Evaluator[Track] {
    override def evaluate(tracks: Seq[Track]): Double =
      tracks.sliding(2).foldLeft(0) { case (acc, t1 +: t2 +: _) => Key.distance(t1.audio.key, t2.audio.key) + acc }
  }
}

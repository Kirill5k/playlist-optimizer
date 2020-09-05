package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.playlist.Key._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class EvaluatorSpec extends AnyWordSpec with Matchers {
  import io.kirill.playlistoptimizer.core.playlist.TrackBuilder._

  "A keyDistanceBasedTracksEvaluator" should {

    "evaluate a sequence of tracks" in {
      val tracks = Vector(
        track("song 1", BMajor),
        track("song 2", EMajor),
        track("song 3", EMajor),
        track("song 4", AMajor),
        track("song 5", DMajor),
        track("song 6", GMajor),
        track("song 7", GMajor),
        track("song 8", GMajor),
        track("song 9", GMajor),
        track("song 10", GMajor)
      )

      Evaluator.keyDistanceBasedTracksEvaluator.evaluate(tracks) must be (4)
    }

    "penalize if tracks are too far apart" in {
      val tracks = Vector(
        track("song 1", EMajor),
        track("song 2", GMinor),
        track("song 3", BFlatMinor),
        track("song 4", EMinor)
      )

      Evaluator.keyDistanceBasedTracksEvaluator.evaluate(tracks) must be (94)
    }
  }
}

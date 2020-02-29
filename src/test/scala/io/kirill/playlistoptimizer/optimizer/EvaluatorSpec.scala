package io.kirill.playlistoptimizer.optimizer

import io.kirill.playlistoptimizer.domain.Key.{AMajor, BMajor, DMajor, EMajor, GMajor}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class EvaluatorSpec extends AnyWordSpec with Matchers {
  import io.kirill.playlistoptimizer.domain.TrackBuilder._

  "A tracksEvaluator" should {

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

      Evaluator.tracksEvaluator.evaluate(tracks) must be (4)
    }
  }
}

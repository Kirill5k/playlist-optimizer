package io.kirill.playlistoptimizer.optimizer.operators

import io.kirill.playlistoptimizer.domain.Key._
import io.kirill.playlistoptimizer.domain.Track
import io.kirill.playlistoptimizer.domain.TrackBuilder.track
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

class MutatorSpec extends AnyWordSpec with Matchers {

  "A randomSwapMutator" should {
    implicit val random = new Random(1)

    "swap 2 random elements in seq" in {
      val s1 = Vector(
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

      val mutatedPlaylist = Mutator.randomSwapMutator[Track].mutate(s1)

      mutatedPlaylist must not contain theSameElementsInOrderAs (s1)
      mutatedPlaylist must contain theSameElementsAs s1
    }
  }
}

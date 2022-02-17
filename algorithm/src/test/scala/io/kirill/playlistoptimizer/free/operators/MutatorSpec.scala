package io.kirill.playlistoptimizer.free.operators

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

class MutatorSpec extends AnyWordSpec with Matchers {

  val tracks = Array("song 1", "song 2", "song 3", "song 4", "song 5", "song 6", "song 7", "song 8", "song 9", "song 10")

  "A randomSwapMutator" should {
    "swap 2 random elements in seq" in {
      given r: Random   = Random(1)
      val mutatedTracks = Mutator.randomSwapMutator[String].mutate(tracks, 0.1)

      mutatedTracks must not contain theSameElementsInOrderAs(tracks)
      mutatedTracks must contain theSameElementsAs tracks
      mutatedTracks must contain theSameElementsInOrderAs
        List("song 1", "song 2", "song 3", "song 4", "song 5", "song 9", "song 7", "song 8", "song 6", "song 10")
    }

    "do multiple swaps when mutation probability is higher" in {
      given r: Random   = Random(1)
      val mutatedTracks = Mutator.randomSwapMutator[String].mutate(tracks, 0.3)

      mutatedTracks must not contain theSameElementsInOrderAs(tracks)
      mutatedTracks must contain theSameElementsAs tracks
      mutatedTracks must contain theSameElementsInOrderAs
        List("song 1", "song 2", "song 3", "song 8", "song 5", "song 9", "song 7", "song 4", "song 6", "song 10")
    }
  }

  "A neighbourSwapMutator" should {
    "swap 2 neighbour elements in seq" in {
      given r: Random   = Random(13)
      val mutatedTracks = Mutator.neighbourSwapMutator[String].mutate(tracks, 0.25)

      mutatedTracks must not contain theSameElementsInOrderAs(tracks)
      mutatedTracks must contain theSameElementsAs tracks
      mutatedTracks must contain theSameElementsInOrderAs
        List("song 1", "song 2", "song 4", "song 3", "song 5", "song 7", "song 6", "song 8", "song 9", "song 10")
    }
  }
}

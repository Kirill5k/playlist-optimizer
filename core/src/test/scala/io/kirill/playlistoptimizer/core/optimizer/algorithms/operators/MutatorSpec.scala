package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.playlist.Track
import io.kirill.playlistoptimizer.core.playlist.Key._
import io.kirill.playlistoptimizer.core.playlist.TrackBuilder.track
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

class MutatorSpec extends AnyWordSpec with Matchers {

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

  "A randomSwapMutator" should {
    "swap 2 random elements in seq" in {
      implicit val r = new Random(1)
      val mutatedTracks = Mutator.randomSwapMutator[Track].mutate(tracks, 1)

      mutatedTracks must not contain theSameElementsInOrderAs (tracks)
      mutatedTracks must contain theSameElementsAs tracks
      mutatedTracks.map(_.song.name) must contain theSameElementsInOrderAs List("song 1", "song 2", "song 3", "song 8", "song 5", "song 6", "song 7", "song 4", "song 9", "song 10")
    }
  }

  "A neighbourSwapMutator" should {
    "swap 2 neighbour elements in seq" in {
      implicit val r = new Random(13)
      val mutatedTracks = Mutator.neighbourSwapMutator[Track].mutate(tracks, 0.25)

      mutatedTracks must not contain theSameElementsInOrderAs (tracks)
      mutatedTracks must contain theSameElementsAs tracks
      mutatedTracks.map(_.song.name) must contain theSameElementsInOrderAs List("song 1", "song 2", "song 4", "song 3", "song 5", "song 7", "song 6", "song 8", "song 9", "song 10")
    }
  }
}

package io.kirill.playlistoptimizer.optimizer

import io.kirill.playlistoptimizer.domain.{Playlist, Song}
import org.scalatest.{FunSpec, Matchers}
import io.kirill.playlistoptimizer.domain._

import scala.util.Random

class OptimizerTest extends FunSpec with Matchers {
  import Key._

  implicit val random = new Random(1)

  describe("Optimizer") {
    val playlist = Playlist(
      Song("song1", "artist", BMajor),
      Song("song2", "artist", EMajor),
      Song("song3", "artist", EMajor),
      Song("song4", "artist", AMajor),
      Song("song5", "artist", DMajor),
      Song("song6", "artist", GMajor),
      Song("song7", "artist", GMajor),
      Song("song8", "artist", GMajor),
      Song("song9", "artist", GMajor),
      Song("song10", "artist", GMajor)
    )

    describe("distributeInPairs") {
      it("distributes elements in list in pairs") {
        val list = List(1,2,3,4,5,6,7,8,9)
        val result = Optimizer.distributeInPairs(list)
        result should contain theSameElementsInOrderAs (List((1,2), (3,4), (5,6), (7,8)))
      }
    }

    describe("evaluate") {
      it("evaluates a playlist") {
        val score = Optimizer.evaluate(playlist)
        assert(score === 4)
      }
    }

    describe("mutate") {
      it("mutates a playlist") {
        val mutatedPlaylist = Optimizer.mutate(playlist)

        mutatedPlaylist.songs should not contain theSameElementsInOrderAs (playlist.songs)
        mutatedPlaylist.songs should contain theSameElementsAs playlist.songs
      }
    }

    describe("crossover") {
      it("crossovers 2 playlists") {
        val playlist2 = playlist.map(Random.shuffle(_))
        val child = Optimizer.crossover(playlist, playlist2)

        child.songs should contain theSameElementsAs playlist.songs
      }
    }
  }
}

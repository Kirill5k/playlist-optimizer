package io.kirill.playlistoptimizer.core.playlist

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits._
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.PlaylistOptimizer.OptimizationId
import org.mockito.scalatest.AsyncMockitoSugar
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.duration._
import scala.util.Random

class PlaylistOptimizerSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with AsyncMockitoSugar {
  implicit val random = new Random(1)

  val playlist = PlaylistBuilder.playlist
  val optimizedTracks = random.shuffle(playlist.tracks)

  "A RefBasedPlaylistOptimizer" - {

    "initiate optimization of a tracks seq" in {
      implicit val alg = mock[OptimizationAlgorithm[IO, Track]]
      val result = for {
        optimizer <- PlaylistOptimizer.refBasedPlaylistOptimizer[IO]
        _ = when(alg.optimizeSeq(playlist.tracks)).thenReturn(IO.sleep(10.seconds) *> IO.pure(optimizedTracks))
        id <- optimizer.optimize(playlist)
      } yield id

      result.asserting(_ mustBe an [OptimizationId])
    }
  }
}

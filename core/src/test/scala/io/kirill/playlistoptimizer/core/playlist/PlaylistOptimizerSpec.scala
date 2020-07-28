package io.kirill.playlistoptimizer.core.playlist

import java.util.UUID

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits._
import io.kirill.playlistoptimizer.core.common.errors.OptimizationNotFound
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

  val optimizationId = OptimizationId(UUID.randomUUID())

  "A RefBasedPlaylistOptimizer" - {

    "initiate optimization of a playlist" in {
      implicit val alg = mock[OptimizationAlgorithm[IO, Track]]
      val result = for {
        optimizer <- PlaylistOptimizer.refBasedPlaylistOptimizer[IO]
        _ = when(alg.optimizeSeq(playlist.tracks)).thenReturn(IO.sleep(10.seconds) *> IO.pure(optimizedTracks))
        id <- optimizer.optimize(playlist)
      } yield id

      result.asserting(_ mustBe an [OptimizationId])
    }

    "return error when optimization id is not recognized" in {
      implicit val alg = mock[OptimizationAlgorithm[IO, Track]]
      val result = for {
        optimizer <- PlaylistOptimizer.refBasedPlaylistOptimizer[IO]
        res <- optimizer.get(optimizationId)
      } yield res

      result.assertThrows[OptimizationNotFound]
    }

    "return incomplete optimization result after if it has not completed" in {
      implicit val alg = mock[OptimizationAlgorithm[IO, Track]]
      val result = for {
        optimizer <- PlaylistOptimizer.refBasedPlaylistOptimizer[IO]
        _ = when(alg.optimizeSeq(playlist.tracks)).thenReturn(IO.sleep(2.seconds) *> IO.pure(optimizedTracks))
        id <- optimizer.optimize(playlist)
        res <- optimizer.get(id)
      } yield res

      result.asserting { optimization =>
        optimization.status must be ("in progress")
        optimization.original must be (playlist)
        optimization.result must be (None)
      }
    }

    "return optimization result after it has completed" in {
      implicit val alg = mock[OptimizationAlgorithm[IO, Track]]
      val result = for {
        optimizer <- PlaylistOptimizer.refBasedPlaylistOptimizer[IO]
        _ = when(alg.optimizeSeq(playlist.tracks)).thenReturn(IO.sleep(2.seconds) *> IO.pure(optimizedTracks))
        id <- optimizer.optimize(playlist)
        _ <- IO.sleep(3.seconds)
        res <- optimizer.get(id)
      } yield res

      result.asserting { optimization =>
        optimization.status must be ("completed")
        optimization.original must be (playlist)
        optimization.result must be (Some(playlist.copy(tracks = optimizedTracks, name = s"Mel optimized")))
      }
    }
  }
}

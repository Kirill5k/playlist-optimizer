package io.kirill.playlistoptimizer.core.optimizer

import cats.effect.IO
import cats.implicits._
import cats.effect.testing.scalatest.AsyncIOSpec
import io.kirill.playlistoptimizer.core.optimizer.Optimizer.OptimizationId
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.optimizer.operators.Evaluator
import io.kirill.playlistoptimizer.core.playlist.{PlaylistBuilder, Track}
import org.mockito.scalatest.AsyncMockitoSugar
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.util.Random
import scala.concurrent.duration._

class OptimizerSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with AsyncMockitoSugar {
  implicit val random = new Random(1)

  val tracks = PlaylistBuilder.playlist.tracks
  val optimizedTracks = random.shuffle(tracks)

  "A RefBasedOptimizer" - {

    "initiate optimization of a tracks seq" in {
      implicit val alg = mock[OptimizationAlgorithm[IO, Track]]
      val result = for {
        optimizer <- Optimizer.refBasedOptimizer[IO, Track]
        _ = when(alg.optimizeSeq(tracks)).thenReturn(IO.sleep(10.seconds) *> IO.pure(optimizedTracks))
        id <- optimizer.optimize(tracks)
      } yield id

      result.asserting(_ mustBe an [OptimizationId])
    }
  }
}

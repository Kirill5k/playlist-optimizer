package io.kirill.playlistoptimizer.core.optimizer.algorithms

import java.time.Instant

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators._
import io.kirill.playlistoptimizer.core.playlist.{PlaylistBuilder, Track}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.util.Random

class GeneticAlgorithmSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val random = new Random(1)

  "A GeneticAlgorithm" - {

    "should optimize a seq of tracks" in {
      val alg = OptimizationAlgorithm.geneticAlgorithm[IO, Track](
        Crossover.bestKeySequenceTrackCrossover,
        Mutator.randomSwapMutator[Track],
        Evaluator.harmonicSeqBasedTracksEvaluator,
        Selector.rouletteWheelSelector[Track],
        Elitism.elitism[Track]
      )

      val start = Instant.now

      val songs  = PlaylistBuilder.playlist.tracks
      val params = OptimizationParameters(200, 250, 0.75, 0.05, 0.2, true)

      val optimizedSongsResult = alg.optimizeSeq(songs, params)

      optimizedSongsResult.asserting { case (tracks, score) =>
        val end = Instant.now()

        println(s"total time taken: ${end.getEpochSecond - start.getEpochSecond}s")

        tracks must contain theSameElementsAs songs
        tracks must not contain theSameElementsInOrderAs(songs)
        score must be < Evaluator.harmonicSeqBasedTracksEvaluator.evaluateIndividual(songs).value / 20
      }
    }
  }
}

package io.kirill.playlistoptimizer.core.optimizer.algorithms

import java.time.Instant

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators._
import io.kirill.playlistoptimizer.core.optimizer.algorithms.Optimizable._
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
        Elitism.simple[Track]
      )

      val start = Instant.now

      val playlist = PlaylistBuilder.playlist
      val params   = OptimizationParameters(200, 250, 0.75, 0.05, 0.2, true)

      val optimizedPlaylist = alg.optimize(playlist, params, _ => IO.unit)

      optimizedPlaylist.asserting { case (result, score) =>
        val end = Instant.now()

        println(s"total time taken: ${end.getEpochSecond - start.getEpochSecond}s")

        result.tracks must contain theSameElementsAs playlist.tracks
        result.tracks must not contain theSameElementsInOrderAs(playlist.tracks)
        score must be < Evaluator.harmonicSeqBasedTracksEvaluator.evaluateIndividual(playlist.repr).value / 20
      }
    }
  }
}

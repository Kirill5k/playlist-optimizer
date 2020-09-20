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
      implicit val c: Crossover[Track] = Crossover.bestKeySequenceTrackCrossover
      implicit val m: Mutator[Track] = Mutator.randomSwapMutator[Track]
      implicit val s: Selector[Track] = Selector.rouletteWheelSelector[Track]
      implicit val e: Elitism[Track] = Elitism.elitism[Track]

      val start = Instant.now

      val songs = PlaylistBuilder.playlist.tracks
      val params = OptimizationParameters(200, 250, 0.75, 0.05, 0.1, true)
      val alg = OptimizationAlgorithm.geneticAlgorithm[IO, Track]
      val optimizedSongsResult = alg.optimizeSeq(songs, params)

      optimizedSongsResult.asserting { case (tracks, score) =>
        val end = Instant.now()

        println(s"total time taken: ${end.getEpochSecond - start.getEpochSecond}s")
        println(s"original score: ${Evaluator.harmonicSeqBasedTracksEvaluator.evaluateIndividual(songs)}, optimized score: ${Evaluator.harmonicSeqBasedTracksEvaluator.evaluateIndividual(tracks)}")
        println(tracks)
        println(keyStreak(tracks))

        tracks must contain theSameElementsAs songs
        tracks must not contain theSameElementsInOrderAs (songs)
        score must be < Evaluator.harmonicSeqBasedTracksEvaluator.evaluateIndividual(songs).value / 20
      }
    }
  }

  def keyStreak(tracks: Seq[Track]): String = {
    tracks
      .map(_.audio.key)
      .map(k => (k.name, s"${k.number}${if (k.mode.number == 0) "A" else "B"}"))
      .mkString(" -> ")
  }
}

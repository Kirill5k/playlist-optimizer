package io.kirill.playlistoptimizer.core.optimizer

import java.time.Instant

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import io.kirill.playlistoptimizer.core.common.config.GeneticAlgorithmConfig
import io.kirill.playlistoptimizer.core.optimizer.operators.{Crossover, Evaluator, Mutator}
import io.kirill.playlistoptimizer.core.playlist.{PlaylistBuilder, Track}
import io.kirill.playlistoptimizer.core.playlist._
import io.kirill.playlistoptimizer.core.optimizer.operators.Crossover
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.util.Random


class OptimizerSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val random = new Random(1)

  "A GeneticAlgorithmOptimizer" - {

    "optimize a playlist" in {
      implicit val c: Crossover[Track] = Crossover.bestKeySequenceTrackCrossover
      implicit val m: Mutator[Track] = Mutator.randomSwapMutator[Track]

      val start = Instant.now

      val config = GeneticAlgorithmConfig(200, 250, 0.3)
      val songs = PlaylistBuilder.playlist.tracks
      val optimizedSongsResult = Optimizer.geneticAlgorithmOptimizer[IO, Track](config).optimize(songs)

      optimizedSongsResult.asserting { tracks =>
        val end = Instant.now()

        println(s"total time taken: ${end.getEpochSecond - start.getEpochSecond}s")
        println(s"original score: ${Evaluator.keyDistanceBasedTracksEvaluator.evaluate(songs)}, optimized score: ${Evaluator.keyDistanceBasedTracksEvaluator.evaluate(tracks)}")
        println(tracks)
        println(keyStreak(tracks))


        tracks must contain theSameElementsAs songs
        tracks must not contain theSameElementsInOrderAs (songs)
        Evaluator.keyDistanceBasedTracksEvaluator.evaluate(tracks) must be < Evaluator.keyDistanceBasedTracksEvaluator.evaluate(songs) / 20
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

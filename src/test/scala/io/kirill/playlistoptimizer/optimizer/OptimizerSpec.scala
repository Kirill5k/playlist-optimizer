package io.kirill.playlistoptimizer.optimizer

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import io.kirill.playlistoptimizer.playlist._
import io.kirill.playlistoptimizer.optimizer.operators.{Crossover, Evaluator, Mutator}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.util.Random


class OptimizerSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val random = new Random(1)

  "A GeneticAlgorithmOptimizer" - {

    "optimize a playlist" in {
      implicit val c: Crossover[Track] = Crossover.bestKeySequenceTrackCrossover
      implicit val m: Mutator[Track] = Mutator.randomSwapMutator[Track]

      val songs = PlaylistBuilder.playlist.tracks
      val optimizedSongsResult = Optimizer.geneticAlgorithmOptimizer[IO, Track](200, 250, 0.3).optimize(songs)

      optimizedSongsResult.asserting { tracks =>
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

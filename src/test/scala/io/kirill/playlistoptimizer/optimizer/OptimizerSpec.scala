package io.kirill.playlistoptimizer.optimizer

import io.kirill.playlistoptimizer.domain.TrackBuilder.track
import io.kirill.playlistoptimizer.domain._
import io.kirill.playlistoptimizer.optimizer.operators.{Crossover, Mutator, Evaluator}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random


class OptimizerSpec extends AnyWordSpec with Matchers {
  import Key._

  implicit val random = new Random(1)

  "An Optimizer" should {
    val s1 = Vector(
      track("s1", BMajor),
      track("s2", EMajor),
      track("s3", EMajor),
      track("s4", AMajor),
      track("s5", DMajor),
      track("s6", GMajor),
      track("s7", GMajor),
      track("s8", GMajor),
      track("s9", GMajor),
      track("s10", GMajor)
    )

    "create initial population" in {
      val population = Optimizer.initPopulation(s1, 10)

      population must have size 10
    }

    "distribute population in pairs" in {
      val s2 = Random.shuffle(s1)
      val s3 = Random.shuffle(s1)
      val s4 = Random.shuffle(s1)
      val distributedInPairs = Optimizer.distributeInPairs(List(s1, s2, s3, s4))

      distributedInPairs must contain allOf ((s1, s2), (s3, s4))
    }
  }

  "A GeneticAlgorithmOptimizer" should {

    "optimize a playlist" in {
      implicit val c: Crossover[Track] = Crossover.bestKeySequenceTrackCrossover
      implicit val m: Mutator[Track] = Mutator.randomSwapMutator[Track]

      val songs = PlaylistBuilder.playlist.tracks
      val optimizedSongs = Optimizer.geneticAlgorithmOptimizer(200, 250, 0.3).optimize(songs)

      optimizedSongs must contain theSameElementsAs songs
      optimizedSongs must not contain theSameElementsInOrderAs (songs)
      Evaluator.keyDistanceBasedTracksEvaluator.evaluate(optimizedSongs) must be < Evaluator.keyDistanceBasedTracksEvaluator.evaluate(songs) / 20

      println(s"original score: ${Evaluator.keyDistanceBasedTracksEvaluator.evaluate(songs)}, optimized score: ${Evaluator.keyDistanceBasedTracksEvaluator.evaluate(optimizedSongs)}")
      println(optimizedSongs)
      println(keyStreak(optimizedSongs))
    }
  }

  def keyStreak(tracks: Seq[Track]): String = {
    tracks
      .map(_.audio.key)
      .map(k => (k.name, s"${k.number}${if (k.mode.number == 0) "A" else "B"}"))
      .mkString(" -> ")
  }
}

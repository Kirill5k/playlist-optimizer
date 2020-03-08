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

  val s1 = Vector(
    track("song 1", BMajor),
    track("song 2", EMajor),
    track("song 3", EMajor),
    track("song 4", AMajor),
    track("song 5", DMajor),
    track("song 6", GMajor),
    track("song 7", GMajor),
    track("song 8", GMajor),
    track("song 9", GMajor),
    track("song 10", GMajor)
  )

  "An Optimizer" should {

    "create initial population" in {
      val population = Optimizer.initPopulation(s1, 10)

      population must have size (10)
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
      implicit val c: Crossover[Track] = Crossover.keySequenceBasedTracksCrossover
      implicit val m: Mutator[Track] = Mutator.neighbourSwapMutator[Track]

      val songs = PlaylistBuilder.playlist.tracks
      val optimizedSongs = Optimizer.geneticAlgorithmOptimizer(200, 250, 0.3).optimize(songs)

      optimizedSongs must contain theSameElementsAs songs
      optimizedSongs must not contain theSameElementsInOrderAs (songs)
      Evaluator.tracksEvaluator.evaluate(optimizedSongs) must be < Evaluator.tracksEvaluator.evaluate(songs) / 4

      println(s"original score: ${Evaluator.tracksEvaluator.evaluate(songs)}, optimized score: ${Evaluator.tracksEvaluator.evaluate(optimizedSongs)}")
      println(optimizedSongs)
    }
  }
}

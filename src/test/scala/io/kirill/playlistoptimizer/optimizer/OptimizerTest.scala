package io.kirill.playlistoptimizer.optimizer

import io.kirill.playlistoptimizer.domain.TrackBuilder.track
import io.kirill.playlistoptimizer.domain._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

class OptimizerTest extends AnyWordSpec with Matchers {
  import Key._

  implicit val random = new Random(1)

  val pl1 = Vector(
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
      val population = Optimizer.initPopulation(pl1, 10)

      population must have size (10)
    }

    "distribute population in pairs" in {
      val pl2 = Random.shuffle(pl1)
      val pl3 = Random.shuffle(pl1)
      val pl4 = Random.shuffle(pl1)
      val distributedInPairs = Optimizer.distributeInPairs(List(pl1, pl2, pl3, pl4))

      distributedInPairs must contain allOf ((pl1, pl2), (pl3, pl4))
    }

    "mutate solution" in {
      val mutatedPlaylist = Optimizer.mutate(pl1)

      mutatedPlaylist must not contain theSameElementsInOrderAs (pl1)
      mutatedPlaylist must contain theSameElementsAs pl1
    }

    "crossover 2 solutions" in {
      val pl2 = Random.shuffle(pl1)
      val child = Optimizer.crossover(pl1, pl2)

      child must contain theSameElementsAs pl1
    }
  }
}

package io.kirill.playlistoptimizer.optimizer

import io.kirill.playlistoptimizer.domain.TrackBuilder.track
import io.kirill.playlistoptimizer.domain._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

class OptimizerTest extends AnyWordSpec with Matchers {
  import Key._

  implicit val random = new Random(1)

  val tracks = Vector(
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
      val population = Optimizer.initPopulation(tracks, 10)

      population must have size (10)
    }
  }
}

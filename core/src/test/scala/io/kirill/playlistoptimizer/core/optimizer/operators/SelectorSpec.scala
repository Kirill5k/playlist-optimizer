package io.kirill.playlistoptimizer.core.optimizer.operators

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

class SelectorSpec extends AnyWordSpec with Matchers {

  "A RouletteWheelSelector" should {

    "sort population by fittest candidates based on probability" in {
      implicit val r = new Random(42)

      val population = Vector((1, 4.0), (2, 2.0), (3, 5.0), (4, 10.0), (5, 1.0))

      val selector = Selector.rouletteWheelSelector[Int]

      val newPopulation = selector.select(population)

      newPopulation must be (Vector(5, 2, 3, 4, 1))
    }
  }
}

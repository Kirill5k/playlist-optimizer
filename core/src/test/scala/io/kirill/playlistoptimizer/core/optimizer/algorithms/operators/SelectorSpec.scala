package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators.operators.Fitness
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

class SelectorSpec extends AnyWordSpec with Matchers {

  "A RouletteWheelSelector" should {

    "sort population by fittest candidates based on probability and distribute them in pairs" in {
      implicit val r = new Random(42)

      val population = List(
        (Vector(1), Fitness(4.0)),
        (Vector(2), Fitness(2.0)),
        (Vector(3), Fitness(5.0)),
        (Vector(4), Fitness(10.0)),
        (Vector(5), Fitness(1.0)),
        (Vector(6), Fitness(20.0))
      )

      val selector = Selector.rouletteWheelSelector[Int]

      val newPopulation = selector.selectPairs(population, 4)

      newPopulation must be (List((Vector(5), Vector(2)), (Vector(3), Vector(4))))
    }
  }
}

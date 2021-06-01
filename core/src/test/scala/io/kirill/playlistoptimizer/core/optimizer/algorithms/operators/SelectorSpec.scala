package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

class SelectorSpec extends AnyWordSpec with Matchers {

  "A FitnessBasedSelector" should {
    "sort population purely by fittest candidates and distribute them in pairs" in {
      implicit val r = new Random(42)

      val population = List(
        (Array(1), Fitness(4.0)),
        (Array(2), Fitness(2.0)),
        (Array(3), Fitness(5.0)),
        (Array(4), Fitness(10.0)),
        (Array(5), Fitness(1.0)),
        (Array(6), Fitness(20.0))
      )

      val selector = Selector.fitnessBasedSelector[Int]

      val newPopulation = selector.selectPairs(population, 4)

      newPopulation.map { case (i1, i2) => (i1.head, i2.head) } mustBe List((5, 2), (1, 3))
    }
  }

  "A RouletteWheelSelector" should {

    "sort population by fittest candidates based on probability and distribute them in pairs" in {
      implicit val r = new Random(42)

      val population = List(
        (Array(1), Fitness(4.0)),
        (Array(2), Fitness(2.0)),
        (Array(3), Fitness(5.0)),
        (Array(4), Fitness(10.0)),
        (Array(5), Fitness(1.0)),
        (Array(6), Fitness(20.0))
      )

      val selector = Selector.rouletteWheelSelector[Int]

      val newPopulation = selector.selectPairs(population, 4)

      newPopulation.map { case (i1, i2) => (i1.head, i2.head) } mustBe List((5, 2), (3, 4))
    }
  }
}

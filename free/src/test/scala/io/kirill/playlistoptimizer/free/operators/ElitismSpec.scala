package io.kirill.playlistoptimizer.free.operators

import io.kirill.playlistoptimizer.free.Fitness
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ElitismSpec extends AnyWordSpec with Matchers {

  "An Elitism operator" should {

    "select proportion of fittest individuals" in {
      val population = List(
        (Array(1), Fitness(4.0)),
        (Array(2), Fitness(2.0)),
        (Array(3), Fitness(5.0)),
        (Array(4), Fitness(10.0)),
        (Array(5), Fitness(1.0)),
        (Array(6), Fitness(20.0))
      )

      val elitism = Elitism.simple[Int]

      val result = elitism.select(population, 6 * 0.35)

      result.map(_.head) mustBe List(5, 2)
    }
  }
}

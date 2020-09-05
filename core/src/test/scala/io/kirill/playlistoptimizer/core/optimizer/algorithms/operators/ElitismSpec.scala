package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ElitismSpec extends AnyWordSpec with Matchers {

  "An Elitism operator" should {

    "select proportion of fittest individuals" in {
      val population = List((1, 4.0), (2, 2.0), (3, 5.0), (4, 10.0), (5, 1.0), (6, 20.0))

      val elitism = Elitism.elitism[Int]

      val result = elitism.select(population, 0.35)

      result must be (List(5, 2))
    }
  }
}

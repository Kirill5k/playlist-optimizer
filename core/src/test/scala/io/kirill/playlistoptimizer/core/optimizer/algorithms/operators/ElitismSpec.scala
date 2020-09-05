package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators.operators.Fitness
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ElitismSpec extends AnyWordSpec with Matchers {

  "An Elitism operator" should {

    "select proportion of fittest individuals" in {
      val population = List(
        (Vector(1), Fitness(4.0)),
        (Vector(2), Fitness(2.0)),
        (Vector(3), Fitness(5.0)),
        (Vector(4), Fitness(10.0)),
        (Vector(5), Fitness(1.0)),
        (Vector(6), Fitness(20.0))
      )

      val elitism = Elitism.elitism[Int]

      val result = elitism.select(population, 0.35)

      result must be (List(Vector(5), Vector(2)))
    }
  }
}

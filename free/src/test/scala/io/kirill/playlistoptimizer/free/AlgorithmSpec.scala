package io.kirill.playlistoptimizer.free

import io.kirill.playlistoptimizer.free.Algorithm.OptimizationParameters
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class AlgorithmSpec extends AnyFreeSpec with Matchers {

  val ind = Array.range(0, 10)
  val params = OptimizationParameters(100, 2, 0.5, 0.2, 0.25, true)

  "A GeneticAlgorithm should" - {
    "optimize a target by applying principles of natural selection" in {

    }
  }
}

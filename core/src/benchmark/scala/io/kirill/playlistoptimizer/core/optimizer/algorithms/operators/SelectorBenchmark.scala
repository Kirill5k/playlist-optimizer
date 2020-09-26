package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.Benchmark
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators.operators.Fitness
import org.scalameter.api._

object SelectorBenchmark extends Benchmark {

  val populations: Gen[Seq[(IndexedSeq[Int], Fitness)]] = evaluatedPopulationGen()

  performance of "rouletteWheelSelector" in {
    val selector = Selector.rouletteWheelSelector[Int]

    measure method "select" in {
      using(populations) config(
        exec.benchRuns -> 10000,
        exec.independentSamples -> 50
      ) in { pop =>
        val result = selector.selectPairs(pop, (pop.size * 0.8).toInt)
      }
    }
  }

  performance of "fitnessBasedSelector" in {
    val selector = Selector.fitnessBasedSelector[Int]

    measure method "select" in {
      using(populations) config(
        exec.benchRuns -> 10000,
        exec.independentSamples -> 50
      ) in { pop =>
        val result = selector.selectPairs(pop, (pop.size * 0.8).toInt)
      }
    }
  }
}
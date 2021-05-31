package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.Benchmark
import org.scalameter.KeyValue
import org.scalameter.api._

object SelectorBenchmark extends Benchmark {

  val populations: Gen[Seq[(IndexedSeq[Int], Fitness)]] = evaluatedPopulationGen()

  performance of "rouletteWheelSelector" in {
    val selector = Selector.rouletteWheelSelector[Int]

    measure method "select" in {
      val ctx = Context(KeyValue(exec.benchRuns -> 10000), KeyValue(exec.independentSamples -> 50))
      using(populations).config(ctx).in { pop =>
        val _ = selector.selectPairs(pop, (pop.size * 0.8).toInt)
      }
    }
  }

  performance of "fitnessBasedSelector" in {
    val selector = Selector.fitnessBasedSelector[Int]

    measure method "select" in {
      val ctx = Context(KeyValue(exec.benchRuns -> 10000), KeyValue(exec.independentSamples -> 50))
      using(populations).config(ctx).in { pop =>
        val _ = selector.selectPairs(pop, (pop.size * 0.8).toInt)
      }
    }
  }
}

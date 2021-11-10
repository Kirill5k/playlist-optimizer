package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.Benchmark
import org.scalameter.KeyValue
import org.scalameter.api._

object ElitismBenchmark extends Benchmark  {

  val populations: Gen[Seq[(Array[Int], Fitness)]] = evaluatedPopulationGen()

  performance of "elitism" in {
    val elitism = Elitism.simple[Int]

    measure method "select" in {
      val ctx = Context(KeyValue(exec.benchRuns -> 10000), KeyValue(exec.independentSamples -> 50))
      using(populations).config(ctx).in { pop =>
        val _ = elitism.select(pop, 0.2)
      }
    }
  }
}

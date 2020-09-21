package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.Benchmark
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators.operators.Fitness
import io.kirill.playlistoptimizer.core.playlist.{Playlist, Track}
import org.scalameter.api._

object ElitismBenchmark extends Benchmark  {

  val populations: Gen[Seq[(IndexedSeq[Int], Fitness)]] = evaluatedPopulationGen()

  performance of "elitism" in {
    val elitism = Elitism.elitism[Int]

    measure method "select" in {
      using(populations) config (
        exec.benchRuns -> 10000,
        exec.independentSamples -> 50
      ) in { pop =>
        val result = elitism.select(pop, 0.2)
      }
    }
  }
}

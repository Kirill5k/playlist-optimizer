package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import io.kirill.playlistoptimizer.core.Benchmark
import io.kirill.playlistoptimizer.domain.optimization.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators.{Crossover, Elitism, Evaluator, Mutator, Selector}
import io.kirill.playlistoptimizer.domain.playlist.Track
import org.scalameter.KeyValue
import org.scalameter.api.*

object GeneticAlgorithmBenchmark extends Benchmark {
  given rt: IORuntime = IORuntime.global

  val playlists = playlistGen(500, 500, 50)

  performance of "GeneticAlgorithm with fitnessBasedSelector" in {

    val params = OptimizationParameters(50, 250, 0.75, 0.1, 0.1, true)
    val algorithm = OptimizationAlgorithm.geneticAlgorithm[IO, Track](
      Crossover.bestKeySequenceTrackCrossover,
      Mutator.randomSwapMutator[Track],
      Evaluator.harmonicSeqBasedTracksEvaluator,
      Selector.fitnessBasedSelector[Track],
      Elitism.simple[Track]
    )

    measure method "optimize" in {
      val ctx = Context(KeyValue(exec.benchRuns -> 250), KeyValue(exec.independentSamples -> 10))
      using(playlists).config(ctx).in { pl =>
        val _ = algorithm.optimize(pl, params, _ => IO.unit).unsafeRunSync()
      }
    }
  }
}

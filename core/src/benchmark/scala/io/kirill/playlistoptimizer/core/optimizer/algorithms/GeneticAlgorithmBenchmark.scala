package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.IO
import io.kirill.playlistoptimizer.core.Benchmark
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators.{Crossover, Elitism, Evaluator, Mutator, Selector}
import io.kirill.playlistoptimizer.core.playlist.Track
import org.scalameter.api._

import scala.concurrent.ExecutionContext

object GeneticAlgorithmBenchmark extends Benchmark {
  implicit val cs = IO.contextShift(ExecutionContext.global)

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

    measure method "optimizeSeq" in {
      val ctx = Context(exec.benchRuns -> 250, exec.independentSamples -> 10)
      using(playlists).config(ctx).in { pl =>
        val _ = algorithm.optimizeSeq(pl.tracks, params).unsafeRunSync()
      }
    }
  }
}

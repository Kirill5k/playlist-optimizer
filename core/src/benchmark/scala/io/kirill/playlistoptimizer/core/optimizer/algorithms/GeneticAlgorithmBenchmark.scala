package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.IO
import io.kirill.playlistoptimizer.core.Benchmark
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators.{Crossover, Elitism, Mutator, Selector, operators}
import io.kirill.playlistoptimizer.core.playlist.Track
import org.scalameter.api._

import scala.concurrent.ExecutionContext

object GeneticAlgorithmBenchmark extends Benchmark {
  implicit val cs = IO.contextShift(ExecutionContext.global)

  val playlists = playlistGen(500, 500, 50)

  performance of "GeneticAlgorithm with fitnessBasedSelector" in {
    implicit val c: Crossover[Track] = Crossover.bestKeySequenceTrackCrossover
    implicit val m: Mutator[Track] = Mutator.randomSwapMutator[Track]
    implicit val s: Selector[Track] = Selector.fitnessBasedSelector[Track]
    implicit val e: Elitism[Track] = Elitism.elitism[Track]
    val params = OptimizationParameters(50, 250, 0.75, 0.1, 0.1, true)
    val algorithm = OptimizationAlgorithm.geneticAlgorithm[IO, Track]

    measure method "optimizeSeq" in {
      using(playlists) config (
        exec.benchRuns -> 250,
        exec.independentSamples -> 10
      ) in { pl =>
        val fitness = algorithm.optimizeSeq(pl.tracks, params).unsafeRunSync()
      }
    }
  }
}

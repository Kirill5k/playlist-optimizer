package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.Benchmark
import io.kirill.playlistoptimizer.domain.playlist.Playlist
import io.kirill.playlistoptimizer.core.optimizer.algorithms.Optimizable.*
import org.scalameter.KeyValue
import org.scalameter.api.*

object EvaluatorBenchmark extends Benchmark  {

  val playlists: Gen[Playlist] = playlistGen()

  performance of "harmonicSeqBasedTracksEvaluator" in {
    val evaluator = Evaluator.harmonicSeqBasedTracksEvaluator

    measure method "evaluate" in {
      val ctx = Context(KeyValue(exec.benchRuns -> 25000), KeyValue(exec.independentSamples -> 100))
      using(playlists).config(ctx).in { pl =>
        val _ = evaluator.evaluateIndividual(pl.repr)
      }
    }
  }
}

package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.Benchmark
import io.kirill.playlistoptimizer.core.playlist.Playlist
import org.scalameter.api._

object EvaluatorBenchmark extends Benchmark  {

  val playlists: Gen[Playlist] = playlistGen()

  performance of "harmonicSeqBasedTracksEvaluator" in {
    val evaluator = Evaluator.harmonicSeqBasedTracksEvaluator

    measure method "evaluate" in {
      val ctx = Context(exec.benchRuns -> 10000, exec.independentSamples -> 50)
      using(playlists).config(ctx).in { pl =>
        val _ = evaluator.evaluateIndividual(pl.tracks)
      }
    }
  }
}

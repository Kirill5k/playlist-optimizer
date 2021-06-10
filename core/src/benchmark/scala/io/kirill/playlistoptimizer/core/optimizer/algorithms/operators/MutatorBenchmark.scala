package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.Benchmark
import io.kirill.playlistoptimizer.core.playlist.{Playlist, Track}
import io.kirill.playlistoptimizer.core.optimizer.algorithms.Optimizable.*
import org.scalameter.{Context, KeyValue}
import org.scalameter.api.*

object MutatorBenchmark extends Benchmark {

  val mutationFactors: Gen[Double] = probabilityGen("mutationFactor")
  val playlists: Gen[Playlist] = playlistGen()

  val playlistsVsMutation = Gen.crossProduct(playlists, mutationFactors)

  performance of "randomSwapMutator" in {
    val mutator = Mutator.randomSwapMutator[Track]

    measure method "mutate" in {
      val ctx = Context(KeyValue(exec.benchRuns -> 2500), KeyValue(exec.independentSamples -> 50))
      using(playlistsVsMutation).config(ctx).in { case (pl, mutationFactor) =>
        val _ = mutator.mutate(pl.repr, mutationFactor)
      }
    }
  }

  performance of "neighbourSwapMutator" in {
    val mutator = Mutator.neighbourSwapMutator[Track]

    measure method "mutate" in {
      val ctx = Context(KeyValue(exec.benchRuns -> 10000), KeyValue(exec.independentSamples -> 100))
      using(playlistsVsMutation).config(ctx).in { case (pl, mutationFactor) =>
        val _ = mutator.mutate(pl.repr, mutationFactor)
      }
    }
  }
}

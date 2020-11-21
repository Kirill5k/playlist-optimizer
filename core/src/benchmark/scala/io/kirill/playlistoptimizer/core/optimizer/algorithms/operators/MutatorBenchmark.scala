package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.Benchmark
import io.kirill.playlistoptimizer.core.playlist.{Playlist, Track}
import org.scalameter.Context
import org.scalameter.api._

object MutatorBenchmark extends Benchmark {

  val mutationFactors: Gen[Double] = probabilityGen("mutationFactor")
  val playlists: Gen[Playlist] = playlistGen()

  val playlistsVsMutation = Gen.crossProduct(playlists, mutationFactors)

  performance of "randomSwapMutator" in {
    val mutator = Mutator.randomSwapMutator[Track]

    measure method "mutate" in {
      val ctx = Context(exec.benchRuns -> 2500, exec.independentSamples -> 50)
      using(playlistsVsMutation).config(ctx).in { case (pl, mutationFactor) =>
        val _ = mutator.mutate(pl.tracks, mutationFactor)
      }
    }
  }

  performance of "neighbourSwapMutator" in {
    val mutator = Mutator.neighbourSwapMutator[Track]

    measure method "mutate" in {
      val ctx = Context(exec.benchRuns -> 2500, exec.independentSamples -> 50)
      using(playlistsVsMutation).config(ctx).in { case (pl, mutationFactor) =>
        val _ = mutator.mutate(pl.tracks, mutationFactor)
      }
    }
  }
}

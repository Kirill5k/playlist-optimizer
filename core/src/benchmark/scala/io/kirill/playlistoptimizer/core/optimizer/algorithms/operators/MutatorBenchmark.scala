package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.{Benchmark, BenchmarkUtils}
import io.kirill.playlistoptimizer.core.playlist.{Playlist, Track}
import org.scalameter.api._

object MutatorBenchmark extends Benchmark {

  val mutationFactors: Gen[Double] = probabilityGen("mutationFactor")
  val playlists: Gen[Playlist] = playlistGen(50, 1000, 50)

  val playlistsVsMutation = Gen.crossProduct(playlists, mutationFactors)

  performance of "randomSwapMutator" in {
    val mutator = Mutator.randomSwapMutator[Track]

    measure method "mutate" in {
      using(playlistsVsMutation) config (
        exec.benchRuns -> 1000,
        exec.independentSamples -> 20
      ) in { case (pl, mutationFactor) =>
        val res = mutator.mutate(pl.tracks, mutationFactor)
      }
    }
  }

  performance of "neighbourSwapMutator" in {
    val mutator = Mutator.neighbourSwapMutator[Track]

    measure method "mutate" in {
      using(playlistsVsMutation) config (
        exec.benchRuns -> 1000,
        exec.independentSamples -> 20
      ) in { case (pl, mutationFactor) =>
        val res = mutator.mutate(pl.tracks, mutationFactor)
      }
    }
  }
}

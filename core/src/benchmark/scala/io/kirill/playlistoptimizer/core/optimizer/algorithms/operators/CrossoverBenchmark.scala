package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.Benchmark
import io.kirill.playlistoptimizer.core.playlist.{Playlist, Track}
import org.scalameter.api._

object CrossoverBenchmark extends Benchmark {

  val playlists: Gen[Playlist] = playlistGen()

  performance of "threeWaySplitCrossover" in {
    val crossover = Crossover.threeWaySplitCrossover[Track]

    measure method "cross" in {
      using(playlists) config (
        exec.benchRuns -> 2500,
        exec.independentSamples -> 50
      ) in { pl =>
        val res = crossover.cross(pl.tracks, rand.shuffle(pl.tracks))
      }
    }
  }

  performance of "bestKeySequenceTrackCrossover" in {
    val crossover = Crossover.bestKeySequenceTrackCrossover

    measure method "cross" in {
      using(playlists) config (
        exec.benchRuns -> 2500,
        exec.independentSamples -> 50
      ) in { pl =>
        val res = crossover.cross(pl.tracks, rand.shuffle(pl.tracks))
      }
    }
  }
}

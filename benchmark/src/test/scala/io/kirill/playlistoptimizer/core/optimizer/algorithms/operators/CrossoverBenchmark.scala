package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.Benchmark
import io.kirill.playlistoptimizer.core.optimizer.algorithms.Optimizable._
import io.kirill.playlistoptimizer.core.playlist.{Playlist, Track}
import org.scalameter.KeyValue
import org.scalameter.api._

object CrossoverBenchmark extends Benchmark {

  val playlists: Gen[Playlist] = playlistGen()

  performance of "threeWaySplitCrossover" in {
    val crossover = Crossover.threeWaySplitCrossover[Track]

    measure method "cross" in {
      val ctx = Context(KeyValue(exec.benchRuns -> 10000), KeyValue(exec.independentSamples -> 100))
      using(playlists).config(ctx).in { pl =>
        val _ = crossover.cross(pl.repr, rand.shuffle(pl.tracks).toArray)
      }
    }
  }

  performance of "bestKeySequenceTrackCrossover" in {
    val crossover = Crossover.bestKeySequenceTrackCrossover

    measure method "cross" in {
      val ctx = Context(KeyValue(exec.benchRuns -> 10000), KeyValue(exec.independentSamples -> 100))
      using(playlists).config(ctx).in { pl =>
        val _ = crossover.cross(pl.repr, rand.shuffle(pl.tracks).toArray)
      }
    }
  }
}

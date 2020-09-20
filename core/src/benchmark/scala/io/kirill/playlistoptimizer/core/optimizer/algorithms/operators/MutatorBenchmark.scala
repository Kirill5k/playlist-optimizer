package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.BenchmarkUtils
import io.kirill.playlistoptimizer.core.playlist.{Playlist, Track}
import org.scalameter.api._

import scala.util.Random

object MutatorBenchmark extends Bench.LocalTime {
  import BenchmarkUtils._

  implicit val rand = Random

  override lazy val reporter = Reporter.Composite(
    new RegressionReporter(
      RegressionReporter.Tester.OverlapIntervals(),
      RegressionReporter.Historian.ExponentialBackoff()
    ),
    HtmlReporter(true)
  )

  val mutationFactors: Gen[Double] =
    Gen.range("mutationFactor")(5, 100, 5).map(_ / 100.0)
  val playlists: Gen[Playlist] =
    Gen.range("playlist")(50, 1000, 50).map(randomizedPlaylist _)

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

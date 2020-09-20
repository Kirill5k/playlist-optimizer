package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.BenchmarkUtils
import io.kirill.playlistoptimizer.core.playlist.Playlist
import org.scalameter.api._

import scala.util.Random

object EvaluatorBenchmark extends Bench.LocalTime {
  import BenchmarkUtils._

  implicit val rand = Random

  override lazy val reporter = Reporter.Composite(
    new RegressionReporter(
      RegressionReporter.Tester.OverlapIntervals(),
      RegressionReporter.Historian.ExponentialBackoff()
    ),
    HtmlReporter(true)
  )

  val playlists: Gen[Playlist] =
    Gen.range("playlist")(50, 1000, 50).map(randomizedPlaylist _)

  performance of "harmonicSeqBasedTracksEvaluator" in {
    val evaluator = Evaluator.harmonicSeqBasedTracksEvaluator

    measure method "evaluate" in {
      using(playlists) config (
        exec.benchRuns -> 500,
        exec.independentSamples -> 20
      ) in { pl =>
        val fitness = evaluator.evaluateIndividual(pl.tracks)
      }
    }
  }
}

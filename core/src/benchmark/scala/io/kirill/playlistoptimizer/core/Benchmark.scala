package io.kirill.playlistoptimizer.core

import io.kirill.playlistoptimizer.core.BenchmarkUtils.randomizedPlaylist
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators.Fitness
import io.kirill.playlistoptimizer.core.playlist.Playlist
import org.scalameter.api._
import org.scalameter.picklers.Implicits._

import scala.util.Random

trait Benchmark extends Bench[Double] {

  implicit val rand = Random

  @transient override lazy val persistor =
    Persistor.None

  @transient lazy val warmer: Warmer =
    Executor.Warmer.Default()

  @transient lazy val aggregator: Aggregator[Double] =
    Aggregator.median[Double]

  @transient override lazy val measurer: Measurer[Double] =
    Measurer.Default()

  @transient override lazy val executor: Executor[Double] =
    LocalExecutor(warmer, aggregator, measurer)

  @transient override lazy val reporter = Reporter.Composite(
    LoggingReporter[Double](),
    new RegressionReporter(
      RegressionReporter.Tester.OverlapIntervals(),
      RegressionReporter.Historian.ExponentialBackoff()
    ),
    HtmlReporter(true)
  )

  def playlistGen(startSize: Int = 50, endSize: Int = 1000, sizeStep: Int = 50): Gen[Playlist] =
    Gen.range("playlist")(startSize, endSize, sizeStep).map(randomizedPlaylist _)

  def probabilityGen(name: String): Gen[Double] =
    Gen.range(name)(5, 100, 5).map(_ / 100.0)

  def evaluatedPopulationGen(
      startSize: Int = 50,
      endSize: Int = 1000,
      sizeStep: Int = 50
  ): Gen[Seq[(IndexedSeq[Int], Fitness)]] =
    Gen.range("populationSize")(startSize, endSize, sizeStep)
      .map(ps => List.fill(ps)((Vector(0), Fitness(rand.nextDouble()))))
}

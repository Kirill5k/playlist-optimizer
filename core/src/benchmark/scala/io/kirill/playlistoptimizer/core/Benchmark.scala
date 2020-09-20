package io.kirill.playlistoptimizer.core

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
}

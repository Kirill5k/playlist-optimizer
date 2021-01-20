package io.kirill.playlistoptimizer.core.optimizer

import java.time.Instant
import java.util.UUID

import io.kirill.playlistoptimizer.core.playlist.Playlist

import scala.concurrent.duration.FiniteDuration

final case class OptimizationParameters(
    populationSize: Int,
    maxGen: Int,
    crossoverProbability: Double,
    mutationProbability: Double,
    elitismRatio: Double,
    shuffle: Boolean
)

final case class OptimizationId(value: UUID) extends AnyVal

final case class Optimization[A](
    id: OptimizationId,
    status: String,
    parameters: OptimizationParameters,
    original: A,
    dateInitiated: Instant,
    duration: Option[FiniteDuration] = None,
    result: Option[A] = None,
    score: Option[BigDecimal] = None
) {

  def complete(result: A, score: BigDecimal, duration: FiniteDuration): Optimization[A] =
    copy(
      status = "completed",
      result = Some(result),
      score = Some(score),
      duration = Some(duration)
    )
}

object Optimization {
  def init[A](id: OptimizationId, parameters: OptimizationParameters, original: A): Optimization[A] =
    Optimization[A](
      id = id,
      status = "in progress",
      parameters = parameters,
      original = original,
      dateInitiated = Instant.now()
    )
}

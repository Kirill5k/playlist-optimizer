package io.kirill.playlistoptimizer.core.optimizer

import java.time.Instant
import java.util.UUID
import scala.concurrent.duration.*

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
    progress: BigDecimal,
    parameters: OptimizationParameters,
    original: A,
    dateInitiated: Instant,
    duration: Option[FiniteDuration] = None,
    result: Option[A] = None,
    score: Option[BigDecimal] = None
) {

  def complete(result: A, score: BigDecimal): Optimization[A] =
    copy(
      progress = BigDecimal(100),
      result = Some(result),
      score = Some(score),
      duration = Some((Instant.now().toEpochMilli - dateInitiated.toEpochMilli).millis)
    )

  def isExpired(expirationTime: FiniteDuration): Boolean =
    duration.fold(false)(d => dateInitiated.plusNanos(d.toNanos + expirationTime.toNanos).isBefore(Instant.now))
}

object Optimization {
  def init[A](parameters: OptimizationParameters, original: A): Optimization[A] =
    Optimization[A](
      id = OptimizationId(UUID.randomUUID()),
      progress = BigDecimal(0),
      parameters = parameters,
      original = original,
      dateInitiated = Instant.now()
    )
}

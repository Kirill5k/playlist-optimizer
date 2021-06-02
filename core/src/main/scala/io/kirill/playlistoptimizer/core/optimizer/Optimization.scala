package io.kirill.playlistoptimizer.core.optimizer

import io.kirill.playlistoptimizer.core.optimizer.algorithms.Optimizable

import java.time.Instant
import java.util.UUID
import scala.concurrent.duration._

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
    original: Optimizable[A],
    dateInitiated: Instant,
    duration: Option[FiniteDuration] = None,
    result: Option[Optimizable[A]] = None,
    score: Option[BigDecimal] = None
) {

  def complete(result: Optimizable[A], score: BigDecimal): Optimization[A] =
    copy(
      status = "completed",
      result = Some(result),
      score = Some(score),
      duration = Some((Instant.now().toEpochMilli - dateInitiated.toEpochMilli).millis)
    )

  def hasCompletedLessThan(time: FiniteDuration): Boolean =
    duration.fold(true)(d => dateInitiated.plusNanos(d.toNanos + time.toNanos).isAfter(Instant.now))
}

object Optimization {
  def init[A](parameters: OptimizationParameters, original: Optimizable[A]): Optimization[A] =
    Optimization[A](
      id = OptimizationId(UUID.randomUUID()),
      status = "in progress",
      parameters = parameters,
      original = original,
      dateInitiated = Instant.now()
    )
}

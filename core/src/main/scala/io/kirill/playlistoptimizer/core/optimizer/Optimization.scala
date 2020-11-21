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

final case class Optimization(
    id: OptimizationId,
    status: String,
    parameters: OptimizationParameters,
    original: Playlist,
    dateInitiated: Instant,
    duration: Option[FiniteDuration] = None,
    result: Option[Playlist] = None,
    score: Option[BigDecimal] = None
)

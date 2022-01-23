package io.kirill.playlistoptimizer.domain.optimization

import io.circe.Encoder
import java.time.Instant
import java.util.UUID

final case class OptimizationView[V](
    id: UUID,
    progress: BigDecimal,
    parameters: OptimizationParameters,
    dateInitiated: Instant,
    original: V,
    durationMs: Option[Long] = None,
    result: Option[V] = None,
    score: Option[BigDecimal] = None
) derives Encoder.AsObject

object OptimizationView:
  def from[A, V](opt: Optimization[A], mapper: A => V): OptimizationView[V] =
    OptimizationView(
      opt.id.value,
      opt.progress,
      opt.parameters,
      opt.dateInitiated,
      mapper(opt.original),
      opt.duration.map(_.toMillis),
      opt.result.map(mapper),
      opt.score
    )

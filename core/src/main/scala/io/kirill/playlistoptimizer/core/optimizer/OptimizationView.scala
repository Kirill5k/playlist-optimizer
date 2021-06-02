package io.kirill.playlistoptimizer.core.optimizer

import io.kirill.playlistoptimizer.core.optimizer.algorithms.Optimizable

import java.time.Instant
import java.util.UUID

final case class OptimizationView[V](
    id: UUID,
    status: String,
    parameters: OptimizationParameters,
    dateInitiated: Instant,
    original: V,
    durationMs: Option[Long] = None,
    result: Option[V] = None,
    score: Option[BigDecimal] = None
)

object OptimizationView {
  def from[A, V](opt: Optimization[A], mapper: Optimizable[A] => V): OptimizationView[V] =
    OptimizationView(
      opt.id.value,
      opt.status,
      opt.parameters,
      opt.dateInitiated,
      mapper(opt.original),
      opt.duration.map(_.toMillis),
      opt.result.map(mapper),
      opt.score
    )
}

package io.kirill.playlistoptimizer.core.health

import cats.effect.Sync
import cats.implicits._
import io.kirill.playlistoptimizer.core.common.controllers.Controller

final case class Health[F[_]](
    controller: Controller[F]
)

object Health {
  def make[F[_]: Sync]: F[Health[F]] =
    HealthController.make[F].map(Health[F])
}

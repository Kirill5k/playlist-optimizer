package io.kirill.playlistoptimizer.core.health

import cats.effect.Sync
import cats.implicits.*
import io.kirill.playlistoptimizer.core.common.controllers.Controller

trait Health[F[_]] {
  def controller: Controller[F]
}

object Health {
  def make[F[_]: Sync]: F[Health[F]] =
    HealthController.make[F].map { healthController =>
      new Health[F] {
        override def controller: Controller[F] = healthController
      }
    }
}

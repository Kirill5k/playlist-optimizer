package io.kirill.playlistoptimizer.core.health

import cats.Monad
import cats.effect.Sync
import io.circe.generic.auto._
import io.kirill.playlistoptimizer.core.common.controllers.Controller
import io.kirill.playlistoptimizer.core.health.HealthController.AppStatus
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._

class HealthController[F[_]: Sync] extends Controller[F] {

  override def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "health" / "status" =>
        Ok(AppStatus(true))
    }
}

object HealthController {

  final case class AppStatus(status: Boolean)

  def make[F[_]: Sync]: F[Controller[F]] =
    Monad[F].pure(new HealthController[F])
}

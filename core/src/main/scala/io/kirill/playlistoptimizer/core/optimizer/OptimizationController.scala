package io.kirill.playlistoptimizer.core.optimizer

import java.time.Instant

import cats.effect.{ContextShift, Sync}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.common.controllers.AppController
import io.kirill.playlistoptimizer.core.optimizer.OptimizationController.{InitiateOptimizationResponse, OptimizationView}
import io.kirill.playlistoptimizer.core.optimizer.PlaylistOptimizer.{Optimization, OptimizationId}
import io.kirill.playlistoptimizer.core.playlist.PlaylistController.PlaylistView
import io.kirill.playlistoptimizer.core.common.json._
import org.http4s.circe._
import org.http4s.{EntityDecoder, HttpRoutes}

class OptimizationController[F[_]](
    private val playlistOptimizer: PlaylistOptimizer[F]
) extends AppController[F] {

  override def routes(implicit cs: ContextShift[F], s: Sync[F], l: Logger[F]): HttpRoutes[F] = {
    implicit val decoder: EntityDecoder[F, PlaylistView] = jsonOf[F, PlaylistView]
    HttpRoutes.of[F] {
      case req @ POST -> Root / "playlist-optimizations" =>
        withErrorHandling {
          for {
            view           <- req.as[PlaylistView]
            _              <- l.info(s"optimize playlist ${view.name}")
            optimizationId <- playlistOptimizer.optimize(view.toDomain)
            resp           <- Created(InitiateOptimizationResponse(optimizationId).asJson)
          } yield resp
        }
      case GET -> Root / "playlist-optimizations" / UUIDVar(optimizationId) =>
        withErrorHandling {
          for {
            opt  <- playlistOptimizer.get(OptimizationId(optimizationId))
            resp <- Ok(OptimizationView.from(opt).asJson)
          } yield resp
        }
      case GET -> Root / "playlist-optimizations" =>
        withErrorHandling {
          for {
            opts <- playlistOptimizer.getAll()
            resp <- Ok(opts.map(OptimizationView.from).asJson)
          } yield resp
        }
    }
  }
}

object OptimizationController {
  final case class InitiateOptimizationResponse(id: OptimizationId)

  final case class OptimizationView(
      status: String,
      dateInitiated: Instant,
      original: PlaylistView,
      durationMs: Option[Long] = None,
      result: Option[PlaylistView] = None,
      score: Option[Double] = None
  )

  object OptimizationView {
    def from(opt: Optimization): OptimizationView =
      OptimizationView(
        opt.status,
        opt.dateInitiated,
        PlaylistView.from(opt.original),
        opt.duration.map(_.toMillis),
        opt.result.map(PlaylistView.from),
        opt.score
      )
  }

  def make[F[_]: Sync](playlistOptimizer: PlaylistOptimizer[F]): F[OptimizationController[F]] =
    Sync[F].delay(new OptimizationController[F](playlistOptimizer))
}
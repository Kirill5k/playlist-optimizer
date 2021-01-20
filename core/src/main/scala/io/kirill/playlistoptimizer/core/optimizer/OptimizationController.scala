package io.kirill.playlistoptimizer.core.optimizer

import java.time.Instant
import java.util.UUID
import cats.effect.{ContextShift, Sync}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.common.controllers.AppController
import io.kirill.playlistoptimizer.core.optimizer.OptimizationController._
import io.kirill.playlistoptimizer.core.common.json._
import io.kirill.playlistoptimizer.core.playlist.{Playlist, PlaylistView}
import org.http4s.circe._
import org.http4s.HttpRoutes

final class OptimizationController[F[_]](
    private val playlistOptimizer: PlaylistOptimizer[F]
) extends AppController[F] {

  override def routes(implicit cs: ContextShift[F], s: Sync[F], l: Logger[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "playlist-optimizations" =>
        withErrorHandling {
          for {
            userSessionId  <- getUserSessionId(req)
            requestBody    <- req.as[PlaylistOptimizationRequest]
            _              <- l.info(s"optimize playlist ${requestBody.playlist.name} for user ${userSessionId.value}")
            optimizationId <- playlistOptimizer.optimize(userSessionId, requestBody.playlist.toDomain, requestBody.optimizationParameters)
            resp           <- Created(PlaylistOptimizationResponse(optimizationId).asJson)
          } yield resp
        }
      case req @ GET -> Root / "playlist-optimizations" / UUIDVar(optimizationId) =>
        withErrorHandling {
          for {
            userSessionId <- getUserSessionId(req)
            _             <- l.info(s"get playlist optimization ${optimizationId} for user ${userSessionId.value}")
            opt           <- playlistOptimizer.get(userSessionId, OptimizationId(optimizationId))
            resp          <- Ok(OptimizationView.from(opt).asJson)
          } yield resp
        }
      case req @ GET -> Root / "playlist-optimizations" =>
        withErrorHandling {
          for {
            userSessionId <- getUserSessionId(req)
            _             <- l.info(s"get all playlist optimizations for user ${userSessionId.value}")
            opts          <- playlistOptimizer.getAll(userSessionId)
            resp          <- Ok(opts.sortBy(_.dateInitiated).reverse.map(OptimizationView.from).asJson)
          } yield resp
        }
      case req @ DELETE -> Root / "playlist-optimizations" / UUIDVar(optimizationId) =>
        withErrorHandling {
          for {
            userSessionId <- getUserSessionId(req)
            _             <- l.info(s"delete optimization $optimizationId for user ${userSessionId.value}")
            _             <- playlistOptimizer.delete(userSessionId, OptimizationId(optimizationId))
            resp          <- NoContent()
          } yield resp
        }
    }
}

object OptimizationController {
  final case class PlaylistOptimizationRequest(
      playlist: PlaylistView,
      optimizationParameters: OptimizationParameters
  )

  final case class PlaylistOptimizationResponse(id: OptimizationId)

  final case class OptimizationView(
      id: UUID,
      status: String,
      parameters: OptimizationParameters,
      dateInitiated: Instant,
      original: PlaylistView,
      durationMs: Option[Long] = None,
      result: Option[PlaylistView] = None,
      score: Option[BigDecimal] = None
  )

  object OptimizationView {
    def from(opt: Optimization[Playlist]): OptimizationView =
      OptimizationView(
        opt.id.value,
        opt.status,
        opt.parameters,
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

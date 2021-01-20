package io.kirill.playlistoptimizer.core.optimizer

import cats.effect.Sync
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.common.controllers.AppController
import io.kirill.playlistoptimizer.core.common.json._
import io.kirill.playlistoptimizer.core.optimizer.OptimizationController._
import io.kirill.playlistoptimizer.core.playlist.{Playlist, PlaylistView}
import org.http4s.HttpRoutes
import org.http4s.circe._

final class OptimizationController[F[_]](
    private val playlistOptimizer: Optimizer[F, Playlist]
) extends AppController[F] {

  override def routes(implicit F: Sync[F], L: Logger[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "playlist-optimizations" =>
        withErrorHandling {
          for {
            userSessionId  <- getUserSessionId(req)
            requestBody    <- req.as[PlaylistOptimizationRequest]
            _              <- L.info(s"optimize playlist ${requestBody.playlist.name} for user ${userSessionId.value}")
            optimizationId <- playlistOptimizer.optimize(userSessionId, requestBody.playlist.toDomain, requestBody.optimizationParameters)
            resp           <- Created(PlaylistOptimizationResponse(optimizationId).asJson)
          } yield resp
        }
      case req @ GET -> Root / "playlist-optimizations" / UUIDVar(optimizationId) =>
        withErrorHandling {
          for {
            userSessionId <- getUserSessionId(req)
            _             <- L.info(s"get playlist optimization ${optimizationId} for user ${userSessionId.value}")
            opt           <- playlistOptimizer.get(userSessionId, OptimizationId(optimizationId))
            resp          <- Ok(OptimizationView.from(opt, PlaylistView.from).asJson)
          } yield resp
        }
      case req @ GET -> Root / "playlist-optimizations" =>
        withErrorHandling {
          for {
            userSessionId <- getUserSessionId(req)
            _             <- L.info(s"get all playlist optimizations for user ${userSessionId.value}")
            opts          <- playlistOptimizer.getAll(userSessionId)
            resp          <- Ok(opts.sortBy(_.dateInitiated).reverse.map(OptimizationView.from(_, PlaylistView.from)).asJson)
          } yield resp
        }
      case req @ DELETE -> Root / "playlist-optimizations" / UUIDVar(optimizationId) =>
        withErrorHandling {
          for {
            userSessionId <- getUserSessionId(req)
            _             <- L.info(s"delete optimization $optimizationId for user ${userSessionId.value}")
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

  def make[F[_]: Sync](playlistOptimizer: Optimizer[F, Playlist]): F[OptimizationController[F]] =
    Sync[F].delay(new OptimizationController[F](playlistOptimizer))
}

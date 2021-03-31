package io.kirill.playlistoptimizer.core.optimizer

import cats.effect.Sync
import cats.implicits._
import org.typelevel.log4cats.Logger
import io.circe.generic.auto._
import io.kirill.playlistoptimizer.core.common.controllers.Controller
import io.kirill.playlistoptimizer.core.optimizer.OptimizationController._
import io.kirill.playlistoptimizer.core.playlist.{Playlist, PlaylistView}
import org.http4s.HttpRoutes

final class OptimizationController[F[_]](
    private val playlistOptimizer: Optimizer[F, Playlist]
)(implicit
    F: Sync[F],
    logger: Logger[F]
) extends Controller[F] {

  override def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "playlist-optimizations" =>
        withErrorHandling {
          for {
            userSessionId  <- F.fromEither(getUserSessionIdFromCookie(req))
            requestBody    <- req.as[PlaylistOptimizationRequest]
            _              <- logger.info(s"optimize playlist ${requestBody.playlist.name} for user ${userSessionId.value}")
            optimizationId <- playlistOptimizer.optimize(userSessionId, requestBody.playlist.toDomain, requestBody.optimizationParameters)
            resp           <- Created(PlaylistOptimizationResponse(optimizationId))
          } yield resp
        }
      case req @ GET -> Root / "playlist-optimizations" / UUIDVar(optimizationId) =>
        withErrorHandling {
          for {
            userSessionId <- F.fromEither(getUserSessionIdFromCookie(req))
            _             <- logger.info(s"get playlist optimization ${optimizationId} for user ${userSessionId.value}")
            opt           <- playlistOptimizer.get(userSessionId, OptimizationId(optimizationId))
            resp          <- Ok(OptimizationView.from(opt, PlaylistView.from))
          } yield resp
        }
      case req @ GET -> Root / "playlist-optimizations" =>
        withErrorHandling {
          for {
            userSessionId <- F.fromEither(getUserSessionIdFromCookie(req))
            _             <- logger.info(s"get all playlist optimizations for user ${userSessionId.value}")
            opts          <- playlistOptimizer.getAll(userSessionId)
            resp          <- Ok(opts.sortBy(_.dateInitiated).reverse.map(OptimizationView.from(_, PlaylistView.from)))
          } yield resp
        }
      case req @ DELETE -> Root / "playlist-optimizations" / UUIDVar(optimizationId) =>
        withErrorHandling {
          for {
            userSessionId <- F.fromEither(getUserSessionIdFromCookie(req))
            _             <- logger.info(s"delete optimization $optimizationId for user ${userSessionId.value}")
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

  def make[F[_]: Sync: Logger](playlistOptimizer: Optimizer[F, Playlist]): F[OptimizationController[F]] =
    Sync[F].delay(new OptimizationController[F](playlistOptimizer))
}

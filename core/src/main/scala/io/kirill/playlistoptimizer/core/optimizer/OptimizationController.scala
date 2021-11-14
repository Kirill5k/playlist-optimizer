package io.kirill.playlistoptimizer.core.optimizer

import cats.Monad
import cats.effect.Async
import cats.implicits.*
import io.circe.generic.auto.*
import io.kirill.playlistoptimizer.core.common.controllers.Controller
import io.kirill.playlistoptimizer.core.optimizer.OptimizationController.*
import io.kirill.playlistoptimizer.core.playlist.{Playlist, PlaylistView, Track}
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.*
import org.typelevel.log4cats.Logger

import java.time.Instant

final class OptimizationController[F[_]](
    private val optimizer: Optimizer[F, Playlist, Track]
)(implicit
    F: Async[F],
    logger: Logger[F]
) extends Controller[F] {

  override def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "playlist-optimizations" =>
        withErrorHandling {
          for {
            userSessionId  <- F.fromEither(getUserSessionIdFromCookie(req))
            requestBody    <- req.as[PlaylistOptimizationRequest]
            _              <- logger.info(s"optimize playlist ${requestBody.playlist.name} for user $userSessionId")
            optimizationId <- optimizer.optimize(userSessionId, requestBody.playlist.toDomain, requestBody.optimizationParameters)
            resp           <- Created(PlaylistOptimizationResponse(optimizationId))
          } yield resp
        }
      case req @ GET -> Root / "playlist-optimizations" / UUIDVar(optimizationId) =>
        withErrorHandling {
          for {
            userSessionId <- F.fromEither(getUserSessionIdFromCookie(req))
            _             <- logger.info(s"get playlist optimization $optimizationId for user $userSessionId")
            opt           <- optimizer.get(userSessionId, OptimizationId(optimizationId))
            resp          <- Ok(OptimizationView.from(opt, PlaylistView.from))
          } yield resp
        }
      case req @ GET -> Root / "playlist-optimizations" =>
        withErrorHandling {
          for {
            userSessionId <- F.fromEither(getUserSessionIdFromCookie(req))
            _             <- logger.info(s"get all playlist optimizations for user $userSessionId")
            opts          <- optimizer.getAll(userSessionId)
            sortedOpts = opts.sortBy(_.dateInitiated)(Ordering[Instant].reverse)
            resp <- Ok(sortedOpts.map(OptimizationView.from(_, PlaylistView.from)))
          } yield resp
        }
      case req @ DELETE -> Root / "playlist-optimizations" / UUIDVar(optimizationId) =>
        withErrorHandling {
          for {
            userSessionId <- F.fromEither(getUserSessionIdFromCookie(req))
            _             <- logger.info(s"delete optimization $optimizationId for user $userSessionId")
            _             <- optimizer.delete(userSessionId, OptimizationId(optimizationId))
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

  def make[F[_]: Async: Logger](playlistOptimizer: Optimizer[F, Playlist, Track]): F[OptimizationController[F]] =
    Monad[F].pure(new OptimizationController[F](playlistOptimizer))
}

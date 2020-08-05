package io.kirill.playlistoptimizer.core.optimizer

import java.time.Instant
import java.util.UUID

import cats.effect.{ContextShift, Sync}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.common.controllers.AppController
import io.kirill.playlistoptimizer.core.optimizer.OptimizationController._
import io.kirill.playlistoptimizer.core.optimizer.PlaylistOptimizer.{Optimization, OptimizationId, OptimizationParameters}
import io.kirill.playlistoptimizer.core.common.json._
import io.kirill.playlistoptimizer.core.playlist.PlaylistView
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
            requestBody    <- req.as[PlaylistOptimizationRequest]
            _              <- l.info(s"optimize playlist ${requestBody.playlist.name}")
            optimizationId <- playlistOptimizer.optimize(requestBody.playlist.toDomain, requestBody.optimizationParameters)
            resp           <- Created(PlaylistOptimizationResponse(optimizationId).asJson)
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
            resp <- Ok(opts.sortBy(_.dateInitiated).reverse.map(OptimizationView.from).asJson)
          } yield resp
        }
      case DELETE -> Root / "playlist-optimizations" / UUIDVar(optimizationId) =>
        withErrorHandling {
          for {
            _    <- l.info(s"delete optimization $optimizationId")
            _    <- playlistOptimizer.delete(OptimizationId(optimizationId))
            resp <- NoContent()
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
      dateInitiated: Instant,
      original: PlaylistView,
      durationMs: Option[Long] = None,
      result: Option[PlaylistView] = None,
      score: Option[Double] = None
  )

  object OptimizationView {
    def from(opt: Optimization): OptimizationView =
      OptimizationView(
        opt.id.value,
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

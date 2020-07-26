package io.kirill.playlistoptimizer.core.common.controllers

import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.Logger
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.common.config.{AppConfig, SpotifyConfig}
import io.kirill.playlistoptimizer.core.common.errors.AuthenticationRequiredError
import io.kirill.playlistoptimizer.core.spotify.{SpotifyPlaylistController, SpotifyPlaylistService}
import io.kirill.playlistoptimizer.core.spotify.SpotifyPlaylistService
import org.http4s.{HttpRoutes, MessageFailure, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._


trait AppController[F[_]] extends Http4sDsl[F] {
  import AppController._

  protected val logger: Logger

  def routes(implicit cs: ContextShift[F], s: Sync[F]): HttpRoutes[F]

  protected def withErrorHandling(response: => F[Response[F]])(implicit s: Sync[F]): F[Response[F]] =
    response.handleErrorWith {
      case AuthenticationRequiredError(message) =>
        logger.error(s"authentication error: ${message}")
        Forbidden(ErrorResponse(message).asJson)
      case error: MessageFailure =>
        logger.error(s"error parsing json: ${error.getMessage()}", error)
        BadRequest(ErrorResponse(error.getMessage()).asJson)
      case error =>
        logger.error(s"unexpected error: ${error.getMessage}", error)
        InternalServerError(ErrorResponse(error.getMessage()).asJson)
    }
}

object AppController {
  final case class ErrorResponse(message: String)

  def homeController(blocker: Blocker)(implicit cs: ContextShift[IO]): AppController[IO] =
    new HomeController[IO](blocker)

  def spotifyController(spotifyService: SpotifyPlaylistService)(implicit config: AppConfig): AppController[IO] = {
    implicit val sc: SpotifyConfig = config.spotify
    new SpotifyPlaylistController(spotifyService)
  }
}

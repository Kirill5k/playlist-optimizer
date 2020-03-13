package io.kirill.playlistoptimizer.controllers

import cats.effect._
import cats.implicits._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.configs.{AppConfig, SpotifyConfig}
import io.kirill.playlistoptimizer.spotify.SpotifyPlaylistController
import org.http4s.{EntityDecoder, HttpRoutes, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import sttp.client.{NothingT, SttpBackend}


trait AppController[F[_]] extends Http4sDsl[F] {
  import AppController._

  def routes(implicit C: ContextShift[F], S: Sync[F]): HttpRoutes[F]

  protected def withErrorHandling(work: => F[Response[F]])(implicit S: Sync[F]): F[Response[F]] =
    work.handleErrorWith { error =>
      InternalServerError(ErrorResponse(error.getMessage()).asJson)
    }
}

object AppController {
  final case class ErrorResponse(message: String)

  def homeController(blocker: Blocker)(implicit cs: ContextShift[IO]): AppController[IO] =
    new HomeController[IO](blocker)

  def spotifyController(config: AppConfig)(implicit cs: ContextShift[IO], B: SttpBackend[IO, Nothing, NothingT]): AppController[IO] = {
    implicit val sc: SpotifyConfig = config.spotify
    new SpotifyPlaylistController
  }
}

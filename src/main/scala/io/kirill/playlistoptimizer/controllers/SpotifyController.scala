package io.kirill.playlistoptimizer.controllers

import cats.effect._
import cats.implicits._
import io.kirill.playlistoptimizer.configs.{AppConfig, SpotifyConfig}
import org.http4s.{HttpRoutes, Response, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location

class SpotifyController[F[_]: Sync](config: AppConfig)(implicit cs: ContextShift[F]) extends AppController[F] {
  implicit val sc: SpotifyConfig = config.spotify

  private val authorizationParams = Map(
    "response_type" -> "code",
    "client_id" -> sc.auth.clientId,
    "scope" -> "playlist-read-private playlist-modify-public playlist-modify-private",
    "redirect_uri" -> s"http://${config.server.hostname}:${config.server.port}/spotify"
  )

  override def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "ping" => Ok("spotify-pong")
      case GET -> Root / "login" => authorizationRedirect
    }

  private val authorizationRedirect: F[Response[F]] =
    TemporaryRedirect(Location(Uri.uri(s"${sc.auth.baseUrl}${sc.auth.authorizationPath}").withQueryParams(authorizationParams)))
}

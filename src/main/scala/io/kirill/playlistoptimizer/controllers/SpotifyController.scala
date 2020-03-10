package io.kirill.playlistoptimizer.controllers

import cats.effect._
import cats.implicits._
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import org.http4s.{HttpRoutes, Uri}
import org.http4s.headers.Location

class SpotifyController[F[_]: Sync](implicit cs: ContextShift[F], sc: SpotifyConfig) extends AppController[F] {

  object CodeQueryParamMatcher extends QueryParamDecoderMatcher[String]("code")

  private val authorizationParams = Map(
    "response_type" -> "code",
    "client_id" -> sc.auth.clientId,
    "scope" -> "playlist-read-private playlist-modify-public playlist-modify-private",
    "redirect_uri" -> sc.auth.redirectUri
  )

  private val authorizationLocation =
    Location(Uri.unsafeFromString(s"${sc.auth.baseUrl}${sc.auth.authorizationPath}").withQueryParams(authorizationParams))

  override def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "ping" => Ok("spotify-pong")
      case GET -> Root / "login" => TemporaryRedirect(authorizationLocation)
      case GET -> Root / "authenticate" :? CodeQueryParamMatcher(code) => Ok(code)
    }
}

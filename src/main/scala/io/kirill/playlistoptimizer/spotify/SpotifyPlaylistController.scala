package io.kirill.playlistoptimizer.spotify

import cats.effect._
import cats.implicits._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import io.kirill.playlistoptimizer.playlist._
import org.http4s.circe._
import org.http4s.headers.Location
import org.http4s.{EntityDecoder, HttpRoutes, Uri}

class SpotifyPlaylistController(implicit sc: SpotifyConfig) extends PlaylistController[IO] {

  private object CodeQueryParamMatcher extends QueryParamDecoderMatcher[String]("code")

  private val authorizationParams = Map(
    "response_type" -> "code",
    "client_id" -> sc.auth.clientId,
    "scope" -> "playlist-read-private playlist-modify-public playlist-modify-private",
    "redirect_uri" -> sc.auth.redirectUri
  )

  private val authorizationLocation =
    Location(Uri.unsafeFromString(s"${sc.auth.baseUrl}${sc.auth.authorizationPath}").withQueryParams(authorizationParams))

  override protected def playlistService: PlaylistService[IO] = ???

  override def routes(implicit C: ContextShift[IO], S: Sync[IO]): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "ping" => Ok("spotify-pong")
      case GET -> Root / "login" => TemporaryRedirect(authorizationLocation)
      case GET -> Root / "authenticate" :? CodeQueryParamMatcher(code) => Ok(code)
    } <+> super.routes
}

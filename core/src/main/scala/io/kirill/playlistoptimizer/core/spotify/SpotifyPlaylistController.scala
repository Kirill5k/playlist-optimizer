package io.kirill.playlistoptimizer.core.spotify

import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.Logger
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.playlist.PlaylistController
import io.kirill.playlistoptimizer.core.playlist._
import org.http4s.circe._
import org.http4s.headers.Location
import org.http4s.{HttpRoutes, Uri}
import sttp.client.{NothingT, SttpBackend}

class SpotifyPlaylistController(override val playlistService: SpotifyPlaylistService)(implicit val sc: SpotifyConfig) extends PlaylistController[IO] {

  protected val logger = Logger[SpotifyPlaylistController]

  private object CodeQueryParamMatcher extends QueryParamDecoderMatcher[String]("code")

  private val authorizationParams = Map(
    "response_type" -> "code",
    "client_id" -> sc.clientId,
    "scope" -> "playlist-read-private playlist-modify-public playlist-modify-private user-read-private user-read-email",
    "redirect_uri" -> sc.redirectUri
  )

  private val authorizationLocation =
    Location(Uri.unsafeFromString(s"${sc.authUrl}${sc.authorizationPath}").withQueryParams(authorizationParams))

  private val homePageLocation =
    Location(Uri.unsafeFromString("/"))

  override def routes(implicit C: ContextShift[IO], S: Sync[IO]): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "ping" =>
        IO(logger.info("spotify ping")) *> Ok("spotify-pong")
      case GET -> Root / "login" =>
        IO(logger.info("redirecting to spotify for authentication")) *> TemporaryRedirect(authorizationLocation)
      case GET -> Root / "authenticate" :? CodeQueryParamMatcher(code) =>
        IO(logger.info(s"received redirect from spotify: $code")) *> playlistService.authenticate(code) *> TemporaryRedirect(homePageLocation)
    } <+> super.routes
}

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

class SpotifyPlaylistController[F[_]](
    override val playlistService: SpotifyPlaylistService[F],
    val spotifyConfig: SpotifyConfig
) extends PlaylistController[F] {

  protected val logger = Logger[SpotifyPlaylistController[F[_]]]

  private object CodeQueryParamMatcher extends QueryParamDecoderMatcher[String]("code")

  private val authorizationParams = Map(
    "response_type" -> "code",
    "client_id"     -> spotifyConfig.clientId,
    "scope"         -> "playlist-read-private playlist-modify-public playlist-modify-private user-read-private user-read-email",
    "redirect_uri"  -> spotifyConfig.redirectUri
  )

  private val authUri =
    Uri.unsafeFromString(s"${spotifyConfig.authUrl}${spotifyConfig.authorizationPath}")
  private val authorizationPath =
    Location(authUri.withQueryParams(authorizationParams))

  private val homePagePath =
    Location(Uri.unsafeFromString("/"))

  override def routes(implicit cs: ContextShift[F], s: Sync[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "ping" =>
        s.delay(logger.info("spotify ping")) *>
          Ok("spotify-pong")
      case GET -> Root / "login" =>
        s.delay(logger.info("redirecting to spotify for authentication")) *>
          TemporaryRedirect(authorizationPath)
      case GET -> Root / "authenticate" :? CodeQueryParamMatcher(code) =>
        s.delay(logger.info(s"received redirect from spotify: $code")) *>
          playlistService.authenticate(code) *>
          TemporaryRedirect(homePagePath)
    } <+> super.routes
}

package io.kirill.playlistoptimizer.core.spotify

import cats.effect._
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
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

  override def routes(implicit cs: ContextShift[F], s: Sync[F], l: Logger[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "ping" =>
        l.info("spotify ping") *>
          Ok("spotify-pong")
      case GET -> Root / "login" =>
        l.info("redirecting to spotify for authentication") *>
          TemporaryRedirect(authorizationPath)
      case GET -> Root / "authenticate" :? CodeQueryParamMatcher(code) =>
        l.info(s"received redirect from spotify: $code") *>
          playlistService.authenticate(code) *>
          TemporaryRedirect(homePagePath)
    } <+> super.routes
}

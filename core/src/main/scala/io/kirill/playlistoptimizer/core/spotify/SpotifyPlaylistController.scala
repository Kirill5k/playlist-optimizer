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
    override val playlistService: SpotifyPlaylistService[F]
)(
    implicit val sc: SpotifyConfig
) extends PlaylistController[F] {

  protected val logger = Logger[SpotifyPlaylistController[F[_]]]

  private object CodeQueryParamMatcher extends QueryParamDecoderMatcher[String]("code")

  private val authorizationParams = Map(
    "response_type" -> "code",
    "client_id"     -> sc.clientId,
    "scope"         -> "playlist-read-private playlist-modify-public playlist-modify-private user-read-private user-read-email",
    "redirect_uri"  -> sc.redirectUri
  )

  private val authorizationLocation =
    Location(Uri.unsafeFromString(s"${sc.authUrl}${sc.authorizationPath}").withQueryParams(authorizationParams))

  private val homePageLocation =
    Location(Uri.unsafeFromString("/"))

  override def routes(implicit C: ContextShift[F], S: Sync[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "ping" =>
        S.delay(logger.info("spotify ping")) *>
          Ok("spotify-pong")
      case GET -> Root / "login" =>
        S.delay(logger.info("redirecting to spotify for authentication")) *>
          TemporaryRedirect(authorizationLocation)
      case GET -> Root / "authenticate" :? CodeQueryParamMatcher(code) =>
        S.delay(logger.info(s"received redirect from spotify: $code")) *>
          playlistService.authenticate(code) *>
          TemporaryRedirect(homePageLocation)
    } <+> super.routes
}

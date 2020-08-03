package io.kirill.playlistoptimizer.core.spotify

import cats.effect._
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.controllers.AppController
import io.kirill.playlistoptimizer.core.common.json._
import io.kirill.playlistoptimizer.core.playlist._
import org.http4s.circe._
import org.http4s.headers.Location
import org.http4s.{HttpRoutes, ResponseCookie, Uri}
import pdi.jwt.{JwtAlgorithm, JwtCirce}

class SpotifyPlaylistController[F[_]](
    val playlistService: SpotifyPlaylistService[F],
    val spotifyConfig: SpotifyConfig
) extends AppController[F] {

  private object CodeQueryParamMatcher extends QueryParamDecoderMatcher[String]("code")

  private val authorizationParams = Map(
    "response_type" -> "code",
    "client_id"     -> spotifyConfig.clientId,
    "scope"         -> "playlist-read-private playlist-modify-public playlist-modify-private user-read-private user-read-email",
    "redirect_uri"  -> spotifyConfig.redirectUrl
  )

  private val authUri =
    Uri.unsafeFromString(s"${spotifyConfig.authUrl}/authorize")
  private val authorizationPath =
    Location(authUri.withQueryParams(authorizationParams))

  private val homePagePath =
    Location(Uri.unsafeFromString(spotifyConfig.homepageUrl))

  override def routes(implicit cs: ContextShift[F], s: Sync[F], l: Logger[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "playlists" =>
        withErrorHandling {
          for {
            _         <- l.info("get all playlists")
            playlists <- playlistService.getAll
            views = playlists.map(PlaylistView.from)
            resp <- Ok(views.asJson)
          } yield resp
        }
      case req @ POST -> Root / "playlists" =>
        withErrorHandling {
          for {
            view <- req.as[PlaylistView]
            _    <- l.info(s"save playlist ${view.name}")
            _    <- playlistService.save(view.toDomain)
            resp <- Created()
          } yield resp
        }
      case GET -> Root / "ping" =>
        l.info("spotify ping") *>
          Ok("spotify-pong")
      case GET -> Root / "login" =>
        l.info("redirecting to spotify for authentication") *>
          TemporaryRedirect(authorizationPath)
      case GET -> Root / "authenticate" :? CodeQueryParamMatcher(code) =>
        for {
          _ <- l.info(s"received redirect from spotify: $code")
          spotifyToken <- playlistService.authenticate(code)
          jwtToken <- s.delay(JwtCirce.encode(spotifyToken.asJson, "secret-key", JwtAlgorithm.HS256))
          spotifyCookie = ResponseCookie("spotify-session", jwtToken, httpOnly = true, secure = true)
          res <- TemporaryRedirect(homePagePath).map(_.addCookie(spotifyCookie))
        } yield res
    }
}

object SpotifyPlaylistController {
  def make[F[_]: Sync](
      spotifyService: SpotifyPlaylistService[F],
      spotifyConfig: SpotifyConfig
  ): F[AppController[F]] =
    Sync[F].delay(new SpotifyPlaylistController[F](spotifyService, spotifyConfig))
}

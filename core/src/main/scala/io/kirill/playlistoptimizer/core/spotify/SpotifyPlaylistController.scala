package io.kirill.playlistoptimizer.core.spotify

import cats.effect._
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.controllers.AppController
import io.kirill.playlistoptimizer.core.common.errors.MissingSessionCookie
import io.kirill.playlistoptimizer.core.common.json._
import io.kirill.playlistoptimizer.core.common.jwt.JwtEncoder
import io.kirill.playlistoptimizer.core.playlist._
import io.kirill.playlistoptimizer.core.spotify.clients.SpotifyAuthClient.SpotifyAccessToken
import org.http4s.circe._
import org.http4s.headers.Location
import org.http4s.{HttpRoutes, Request, RequestCookie, ResponseCookie, Uri}
import pdi.jwt.{JwtAlgorithm, JwtCirce}

class SpotifyPlaylistController[F[_]](
    val jwtEncoder: JwtEncoder[F, SpotifyAccessToken],
    val playlistService: SpotifyPlaylistService[F],
    val spotifyConfig: SpotifyConfig
) extends AppController[F] {

  private val SpotifySessionCookie = "spotify-session"

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
      case req @ GET -> Root / "ping" =>
        withErrorHandling {
          for {
            _   <- l.info("spotify ping")
            spotifyCookie <- getSessionCookie[F](req)
            spotifyToken <- jwtEncoder.decode(spotifyCookie.content)
            res <- Ok("spotify-pong")
          } yield res
        }
      case GET -> Root / "login" =>
        l.info("redirecting to spotify for authentication") *>
          TemporaryRedirect(authorizationPath)
      case GET -> Root / "authenticate" :? CodeQueryParamMatcher(code) =>
        for {
          _            <- l.info(s"received redirect from spotify: $code")
          spotifyToken <- playlistService.authenticate(code)
          jwtToken     <- jwtEncoder.encode(spotifyToken)
          res <- TemporaryRedirect(homePagePath).map(_.addCookie(newSessionCookie(jwtToken)))
        } yield res
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
    }

  private def getSessionCookie[F[_]: Sync](req: Request[F]): F[RequestCookie] =
    Sync[F].fromOption(req.cookies.find(_.name == SpotifySessionCookie), MissingSessionCookie)

  private def newSessionCookie(content: String): ResponseCookie =
    ResponseCookie(SpotifySessionCookie, content, httpOnly = true, secure = true)
}

object SpotifyPlaylistController {
  def make[F[_]: Sync](
      jwtEncoder: JwtEncoder[F, SpotifyAccessToken],
      spotifyService: SpotifyPlaylistService[F],
      spotifyConfig: SpotifyConfig
  ): F[AppController[F]] =
    Sync[F].delay(new SpotifyPlaylistController[F](jwtEncoder, spotifyService, spotifyConfig))
}

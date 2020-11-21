package io.kirill.playlistoptimizer.core.spotify

import cats.effect._
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.controllers.AppController
import io.kirill.playlistoptimizer.core.common.errors.{MissingRequiredQueryParam, MissingSpotifySessionCookie}
import io.kirill.playlistoptimizer.core.common.json._
import io.kirill.playlistoptimizer.core.common.jwt.JwtEncoder
import io.kirill.playlistoptimizer.core.playlist._
import org.http4s.circe._
import org.http4s.dsl.io.{OptionalQueryParamDecoderMatcher, QueryParamDecoderMatcher}
import org.http4s.headers.Location
import org.http4s.{HttpRoutes, Request, RequestCookie, ResponseCookie, Uri}

final class SpotifyPlaylistController[F[_]](
    val jwtEncoder: JwtEncoder[F, SpotifyAccessToken],
    val playlistService: SpotifyPlaylistService[F],
    val spotifyConfig: SpotifyConfig
) extends AppController[F] {
  import SpotifyPlaylistController._

  private val authorizationParams = Map(
    "response_type" -> "code",
    "client_id"     -> spotifyConfig.clientId,
    "scope"         -> "playlist-read-private playlist-modify-public playlist-modify-private user-read-private user-read-email",
    "redirect_uri"  -> spotifyConfig.redirectUrl,
    "show_dialog" -> "true"
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
            _             <- l.info("spotify ping")
            spotifyCookie <- getSpotifySessionCookie(req)
            _             <- jwtEncoder.decode(spotifyCookie.content)
            res           <- Ok("spotify-pong")
          } yield res
        }
      case GET -> Root / "login" =>
        l.info("redirecting to spotify for authentication") *>
          TemporaryRedirect(authorizationPath)
      case GET -> Root / "logout" =>
        l.info("logging out") *>
          TemporaryRedirect(homePagePath).map(_.removeCookie(SpotifySessionCookie))
      case GET -> Root / "authenticate" :? CodeQueryParamMatcher(code) =>
        for {
          _           <- l.info(s"received redirect from spotify: $code")
          accessToken <- playlistService.authenticate(code)
          jwt         <- jwtEncoder.encode(accessToken)
          res         <- TemporaryRedirect(homePagePath)
        } yield res.addCookie(newSessionCookie(jwt))
      case req @ GET -> Root / "tracks" :? TrackQueryParamMatcher(name) =>
        withErrorHandling {
          for {
            _             <- l.info(s"find track $name")
            query         <- Sync[F].fromOption(name, MissingRequiredQueryParam("name"))
            spotifyCookie <- getSpotifySessionCookie(req)
            accessToken   <- jwtEncoder.decode(spotifyCookie.content)
            track         <- playlistService.findTrackByName(accessToken, query)
            jwt           <- jwtEncoder.encode(track._2)
            res           <- Ok(TrackView.from(track._1).asJson)
          } yield res.addCookie(newSessionCookie(jwt))
        }
      case req @ GET -> Root / "playlists" =>
        withErrorHandling {
          for {
            _             <- l.info("get all playlists")
            spotifyCookie <- getSpotifySessionCookie(req)
            accessToken   <- jwtEncoder.decode(spotifyCookie.content)
            playlists     <- playlistService.getAll(accessToken)
            views = playlists._1.map(PlaylistView.from)
            jwt <- jwtEncoder.encode(playlists._2)
            res <- Ok(views.asJson)
          } yield res.addCookie(newSessionCookie(jwt))
        }
      case req @ POST -> Root / "playlists" =>
        withErrorHandling {
          for {
            view               <- req.as[PlaylistView]
            _                  <- l.info(s"save playlist ${view.name}")
            spotifyCookie      <- getSpotifySessionCookie(req)
            accessToken        <- jwtEncoder.decode(spotifyCookie.content)
            updatedAccessToken <- playlistService.save(accessToken, view.toDomain)
            jwt                <- jwtEncoder.encode(updatedAccessToken)
            res                <- Created()
          } yield res.addCookie(newSessionCookie(jwt))
        }
      case req @ POST -> Root / "playlists" / "import" =>
        withErrorHandling {
          for {
            importReq       <- req.as[ImportPlaylistRequest]
            _               <- l.info(s"import playlist ${importReq.name}")
            spotifyCookie   <- getSpotifySessionCookie(req)
            accessToken     <- jwtEncoder.decode(spotifyCookie.content)
            tracksWithToken <- playlistService.findTracksByNames(accessToken, importReq.tracks)
            playlist = Playlist(importReq.name, importReq.description, tracksWithToken._1.toVector, PlaylistSource.Spotify)
            updatedAccessToken <- playlistService.save(tracksWithToken._2, playlist)
            jwt                <- jwtEncoder.encode(updatedAccessToken)
            res                <- Created()
          } yield res.addCookie(newSessionCookie(jwt))
        }
    }

  private def getSpotifySessionCookie(req: Request[F])(implicit s: Sync[F]): F[RequestCookie] =
    s.fromOption(getCookie(req, SpotifySessionCookie), MissingSpotifySessionCookie)

  private def newSessionCookie(jwt: String): ResponseCookie =
    ResponseCookie(SpotifySessionCookie, jwt, httpOnly = true)
}

object SpotifyPlaylistController {
  val SpotifySessionCookie = "spotify-session"

  object CodeQueryParamMatcher  extends QueryParamDecoderMatcher[String]("code")
  object TrackQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("name")

  final case class ImportPlaylistRequest(
      name: String,
      description: Option[String],
      tracks: List[String]
  )

  def make[F[_]: Sync](
      jwtEncoder: JwtEncoder[F, SpotifyAccessToken],
      spotifyService: SpotifyPlaylistService[F],
      spotifyConfig: SpotifyConfig
  ): F[AppController[F]] =
    Sync[F].delay(new SpotifyPlaylistController[F](jwtEncoder, spotifyService, spotifyConfig))
}

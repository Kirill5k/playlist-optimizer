package io.kirill.playlistoptimizer.core.spotify

import java.time.{Instant, LocalDate}
import java.util.UUID

import cats.effect.IO
import io.circe.generic.auto._
import io.circe.literal._
import io.kirill.playlistoptimizer.core.ControllerSpec
import io.kirill.playlistoptimizer.core.common.SpotifyConfigBuilder
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.controllers.AppController.ErrorResponse
import io.kirill.playlistoptimizer.core.common.errors.{AuthenticationRequiredError, JwtDecodeError}
import io.kirill.playlistoptimizer.core.common.json._
import io.kirill.playlistoptimizer.core.common.jwt.JwtEncoder
import io.kirill.playlistoptimizer.core.optimizer.{Optimization, OptimizationId, OptimizationParameters}
import io.kirill.playlistoptimizer.core.playlist._
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.mockito.ArgumentCaptor

class SpotifyPlaylistControllerSpec extends ControllerSpec {

  val sc: SpotifyConfig = SpotifyConfigBuilder.testConfig

  val playlist = PlaylistBuilder.playlist
  val shortenedPlaylist = playlist.copy(tracks = Vector(playlist.tracks.head))

  val optimizationId = OptimizationId(UUID.fromString("607995e0-8e3a-11ea-bc55-0242ac130003"))
  val optimizationParameters = OptimizationParameters(100, 1000, 0.5, 0.2, 0.1, true)
  val optimization = Optimization(optimizationId, "in progress", optimizationParameters, shortenedPlaylist, Instant.parse("2020-01-01T00:00:00Z"))

  val sessionCookie = RequestCookie("spotify-session", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhY2Nlc3NUb2tlbiI6ImFjY2Vzcy10b2tlbiIsInJlZnJlc2hUb2tlbiI6InJlZnJlc2gtdG9rZW4iLCJ1c2VySWQiOiJ1c2VyLWlkIiwidmFsaWRVbnRpbCI6IjIwMjAtMDEtMDFUMDA6MDA6MDBaIn0.e14E3Fp-aJDpcs86HYfGAkUQjQwS9d73YjSHhaIxpUw")
  val accessToken = SpotifyAccessToken("access-token", "refresh-token", "user-id", Instant.parse("2020-01-01T00:00:00Z"))

  "A SpotifyPlaylistController" should {

    "GET /logout" in {
      val (jwtEncoder, service) = mocks
      val controller = new SpotifyPlaylistController(jwtEncoder, service, sc)

      val request = Request[IO](uri = uri"/logout").addCookie(sessionCookie)
      val response: IO[Response[IO]] = controller.routesWithUserSession.orNotFound.run(request)

      val res = response.unsafeRunSync()

      res.status must be (Status.TemporaryRedirect)
      val spotifySessionCookie = res.cookies.find(_.name == "spotify-session").get
      spotifySessionCookie.expires must be (Some(HttpDate.Epoch))
    }

    "return existing user-session cookie" in {
      val (jwtEncoder, service) = mocks
      val controller = new SpotifyPlaylistController(jwtEncoder, service, sc)

      val spotifyAccessToken = SpotifyAccessToken("access-token", "refresh-token", "user-id", Instant.parse("2020-01-01T00:00:00Z"))
      when(service.authenticate(any[String])).thenReturn(IO.pure(spotifyAccessToken))
      when(jwtEncoder.encode(spotifyAccessToken)).thenReturn(IO.pure(sessionCookie.content))

      val request = Request[IO](uri = uri"/authenticate?code=access-code").addCookie(RequestCookie("user-session", "user-id"))
      val response: IO[Response[IO]] = controller.routesWithUserSession.orNotFound.run(request)

      val cookies = response.map(_.cookies).unsafeRunSync()

      cookies.map(_.name) must contain ("user-session")
      cookies.find(_.name == "user-session").map(_.content) must be (Some("user-id"))
    }

    "set user-session cookie" in {
      val (jwtEncoder, service) = mocks
      val controller = new SpotifyPlaylistController(jwtEncoder, service, sc)

      val spotifyAccessToken = SpotifyAccessToken("access-token", "refresh-token", "user-id", Instant.parse("2020-01-01T00:00:00Z"))
      when(service.authenticate(any[String])).thenReturn(IO.pure(spotifyAccessToken))
      when(jwtEncoder.encode(spotifyAccessToken)).thenReturn(IO.pure(sessionCookie.content))

      val request = Request[IO](uri = uri"/authenticate?code=access-code")
      val response: IO[Response[IO]] = controller.routesWithUserSession.orNotFound.run(request)

      val cookies = response.map(_.cookies).unsafeRunSync()

      cookies.map(_.name) must contain ("user-session")
    }

    "set spotify-session cookie on authentication" in {
      val (jwtEncoder, service) = mocks
      val controller = new SpotifyPlaylistController(jwtEncoder, service, sc)

      val spotifyAccessToken = SpotifyAccessToken("access-token", "refresh-token", "user-id", Instant.parse("2020-01-01T00:00:00Z"))
      when(service.authenticate(any[String])).thenReturn(IO.pure(spotifyAccessToken))
      when(jwtEncoder.encode(spotifyAccessToken)).thenReturn(IO.pure(sessionCookie.content))

      val request = Request[IO](uri = uri"/authenticate?code=access-code")
      val response: IO[Response[IO]] = controller.routes.orNotFound.run(request)

      verifyResponse[ErrorResponse](response, Status.TemporaryRedirect, cookies = Map("spotify-session" -> sessionCookie.content))

      verify(service).authenticate("access-code")
    }

    "return forbidden when no spotify-session cookie" in {
      val (jwtEncoder, service) = mocks
      val controller = new SpotifyPlaylistController(jwtEncoder, service, sc)

      when(jwtEncoder.decode(any[String])).thenReturn(IO.raiseError(JwtDecodeError("invalid-token")))

      val request = Request[IO](uri = uri"/ping")
      val response: IO[Response[IO]] = controller.routes.orNotFound.run(request)

      verifyResponse[ErrorResponse](response, Status.Forbidden, Some(ErrorResponse("missing spotify session cookie")))
    }

    "return forbidden when invalid spotify-session cookie" in {
      val (jwtEncoder, service) = mocks
      val controller = new SpotifyPlaylistController(jwtEncoder, service, sc)

      when(jwtEncoder.decode(any[String])).thenReturn(IO.raiseError(JwtDecodeError("invalid-token")))

      val request = Request[IO](uri = uri"/ping").addCookie("spotify-session", "foo-bar")
      val response: IO[Response[IO]] = controller.routes.orNotFound.run(request)

      verifyResponse[ErrorResponse](response, Status.Forbidden, Some(ErrorResponse("invalid-token")))
      verify(jwtEncoder).decode("foo-bar")
    }

    "GET /tracks - find track by name" in {
      val (jwtEncoder, service) = mocks
      val controller = new SpotifyPlaylistController(jwtEncoder, service, sc)
      when(jwtEncoder.decode(sessionCookie.content)).thenReturn(IO.pure(accessToken))
      when(service.findTrackByName(eqTo(accessToken), any[String])).thenReturn(IO.pure((shortenedPlaylist.tracks.head, accessToken)))
      when(jwtEncoder.encode(accessToken)).thenReturn(IO.pure(sessionCookie.content))

      val request = Request[IO](uri = uri"/tracks?name=track-name").addCookie(sessionCookie)
      val response: IO[Response[IO]] = controller.routes.orNotFound.run(request)

      val expected = TrackView("Glue", List("Bicep"), Some("Bicep"), Some(LocalDate.of(2017, 9, 1)), Some("album"), None, 129.983, 269.15, 5, 0, 0.613,0.807, "spotify:track:2aJDlirz6v2a4HREki98cP", Some("https://open.spotify.com/track/2aJDlirz6v2a4HREki98cP"))

      verifyResponse[TrackView](response, Status.Ok, Some(expected), cookies = Map("spotify-session" -> sessionCookie.content))
      verify(jwtEncoder).decode(sessionCookie.content)
      verify(service).findTrackByName(accessToken, "track-name")
      verify(jwtEncoder).encode(accessToken)
    }

    "GET /tracks - return bad request when query param is missing" in {
      val (jwtEncoder, service) = mocks
      val controller = new SpotifyPlaylistController(jwtEncoder, service, sc)

      val request = Request[IO](uri = uri"/tracks").addCookie(sessionCookie)
      val response: IO[Response[IO]] = controller.routes.orNotFound.run(request)

      verifyResponse[ErrorResponse](response, Status.BadRequest, Some(ErrorResponse("query parameter name is required to make this request")))
      verifyZeroInteractions(jwtEncoder, service)
    }

    "GET /playlists - get all playlists" in {
      val (jwtEncoder, service) = mocks
      val controller = new SpotifyPlaylistController(jwtEncoder, service, sc)
      when(jwtEncoder.decode(sessionCookie.content)).thenReturn(IO.pure(accessToken))
      when(service.getAll(accessToken)).thenReturn(IO.pure((List(shortenedPlaylist), accessToken)))
      when(jwtEncoder.encode(accessToken)).thenReturn(IO.pure(sessionCookie.content))

      val request = Request[IO](uri = uri"/playlists").addCookie(sessionCookie)
      val response: IO[Response[IO]] = controller.routes.orNotFound.run(request)

      val expected = List(PlaylistView(
        "Mel",
        Some("Melodic deep house and techno songs"),
        List(TrackView("Glue", List("Bicep"), Some("Bicep"), Some(LocalDate.of(2017, 9, 1)), Some("album"), None, 129.983, 269.15, 5, 0, 0.613,0.807, "spotify:track:2aJDlirz6v2a4HREki98cP", Some("https://open.spotify.com/track/2aJDlirz6v2a4HREki98cP"))),
        "Spotify"
      ))

      verifyResponse[Seq[PlaylistView]](response, Status.Ok, Some(expected), cookies = Map("spotify-session" -> sessionCookie.content))
      verify(jwtEncoder).decode(sessionCookie.content)
      verify(service).getAll(accessToken)
      verify(jwtEncoder).encode(accessToken)
    }

    "POST /playlists - create new playlist" in {
      val (jwtEncoder, service) = mocks
      val controller = new SpotifyPlaylistController(jwtEncoder, service, sc)
      val playlistCaptor: ArgumentCaptor[Playlist] = ArgumentCaptor.forClass(classOf[Playlist])
      when(jwtEncoder.decode(sessionCookie.content)).thenReturn(IO.pure(accessToken))
      when(service.save(eqTo(accessToken),playlistCaptor.capture())).thenReturn(IO.pure(accessToken))
      when(jwtEncoder.encode(accessToken)).thenReturn(IO.pure(sessionCookie.content))

      val request = Request[IO](uri = uri"/playlists", method = Method.POST).withEntity(shortenedPlaylistJson).addCookie(sessionCookie)
      val response: IO[Response[IO]] = controller.routes.orNotFound.run(request)

      verifyResponse[PlaylistView](response, Status.Created, cookies = Map("spotify-session" -> sessionCookie.content))
      verify(jwtEncoder).decode(sessionCookie.content)
      playlistCaptor.getValue must be (shortenedPlaylist)
      verify(jwtEncoder).encode(accessToken)
    }

    "return internal server error if unexpected error" in {
      val (jwtEncoder, service) = mocks
      val controller = new SpotifyPlaylistController(jwtEncoder, service, sc)
      when(jwtEncoder.decode(sessionCookie.content)).thenReturn(IO.pure(accessToken))
      when(service.getAll(accessToken)).thenReturn(IO.raiseError(new NullPointerException("error-message")))

      val request = Request[IO](uri = uri"/playlists").addCookie(sessionCookie)
      val response: IO[Response[IO]] = controller.routes.orNotFound.run(request)

      verifyResponse[ErrorResponse](response, Status.InternalServerError, Some(ErrorResponse("error-message")))
    }

    "return error when not authenticated" in {
      val (jwtEncoder, service) = mocks
      val controller = new SpotifyPlaylistController(jwtEncoder, service, sc)
      when(jwtEncoder.decode(sessionCookie.content)).thenReturn(IO.pure(accessToken))
      when(service.getAll(accessToken)).thenReturn(IO.raiseError(AuthenticationRequiredError("authorization with Spotify is required")))

      val request = Request[IO](uri = uri"/playlists").addCookie(sessionCookie)
      val response: IO[Response[IO]] = controller.routes.orNotFound.run(request)

      verifyResponse[ErrorResponse](response, Status.Forbidden, Some(ErrorResponse("authorization with Spotify is required")))
    }

    "POST /playlists/import - import new playlist into spotify" in {
      val (jwtEncoder, service) = mocks
      val controller = new SpotifyPlaylistController(jwtEncoder, service, sc)
      when(jwtEncoder.decode(sessionCookie.content)).thenReturn(IO.pure(accessToken))
      when(service.findTracksByNames(eqTo(accessToken), anyList[String])).thenReturn(IO.pure((shortenedPlaylist.tracks.toList, accessToken)))
      when(service.save(eqTo(accessToken),any[Playlist])).thenReturn(IO.pure(accessToken))
      when(jwtEncoder.encode(accessToken)).thenReturn(IO.pure(sessionCookie.content))

      val importPlaylistRequest =
        json"""
        {
          "name" : "New Imported playlist",
          "description" : "Imported playlist with songs",
          "tracks" : [
            "bicep - glue",
            "bicep - ayaya",
            "bicep - oval"
          ]
        }
      """

      val request = Request[IO](uri = uri"/playlists/import", method = Method.POST).withEntity(importPlaylistRequest).addCookie(sessionCookie)
      val response: IO[Response[IO]] = controller.routes.orNotFound.run(request)

      verifyResponse[PlaylistView](response, Status.Created, cookies = Map("spotify-session" -> sessionCookie.content))
      verify(jwtEncoder).decode(sessionCookie.content)
      verify(jwtEncoder).encode(accessToken)
      verify(service).findTracksByNames(accessToken, List("bicep - glue", "bicep - ayaya", "bicep - oval"))
      verify(service).save(accessToken, Playlist("New Imported playlist", Some("Imported playlist with songs"), shortenedPlaylist.tracks, PlaylistSource.Spotify))
    }
  }
  
  def mocks: (JwtEncoder[IO, SpotifyAccessToken], SpotifyPlaylistService[IO]) =
    (mock[JwtEncoder[IO, SpotifyAccessToken]], mock[SpotifyPlaylistService[IO]])
}

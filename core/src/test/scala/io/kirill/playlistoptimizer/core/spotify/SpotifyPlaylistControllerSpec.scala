package io.kirill.playlistoptimizer.core.spotify

import java.time.Instant

import cats.effect.{ContextShift, IO}
import io.kirill.playlistoptimizer.core.common.errors.AuthenticationRequiredError
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.ControllerSpec
import io.kirill.playlistoptimizer.core.common.SpotifyConfigBuilder
import io.kirill.playlistoptimizer.core.common.json._
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.controllers.AppController.ErrorResponse
import io.kirill.playlistoptimizer.core.optimizer.PlaylistOptimizer
import io.kirill.playlistoptimizer.core.spotify.clients.SpotifyAuthClient.SpotifyAccessToken
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub

class SpotifyPlaylistControllerSpec extends ControllerSpec {

  implicit val sc: SpotifyConfig = SpotifyConfigBuilder.testConfig

  "A SpotifyPlaylistController" should {

    "return error when not authenticated" in {
      val playlistServiceMock = mock[SpotifyPlaylistService[IO]]
      val controller = new SpotifyPlaylistController(playlistServiceMock, sc)

      when(playlistServiceMock.getAll).thenReturn(IO.raiseError(AuthenticationRequiredError("authorization with Spotify is required")))

      val request = Request[IO](uri = uri"/playlists")
      val response: IO[Response[IO]] = controller.routes.orNotFound.run(request)

      verifyResponse[ErrorResponse](response, Status.Forbidden, Some(ErrorResponse("authorization with Spotify is required")))
    }

    "set spotify-session cookie on authentication" in {
      val playlistServiceMock = mock[SpotifyPlaylistService[IO]]
      val controller = new SpotifyPlaylistController(playlistServiceMock, sc)

      val spotifyAccessToken = SpotifyAccessToken("access-token", "refresh-token", "user-id", Instant.parse("2020-01-01T00:00:00Z"))
      when(playlistServiceMock.authenticate(any[String])).thenReturn(IO.pure(spotifyAccessToken))

      val request = Request[IO](uri = uri"/authenticate?code=access-code")
      val response: IO[Response[IO]] = controller.routes.orNotFound.run(request)

      val cookies = response.map(_.cookies).unsafeRunSync()
      cookies.find(_.name == "spotify-session").map(_.content) must be (Some("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhY2Nlc3NUb2tlbiI6ImFjY2Vzcy10b2tlbiIsInJlZnJlc2hUb2tlbiI6InJlZnJlc2gtdG9rZW4iLCJ1c2VySWQiOiJ1c2VyLWlkIiwidmFsaWRVbnRpbCI6IjIwMjAtMDEtMDFUMDA6MDA6MDBaIn0.e14E3Fp-aJDpcs86HYfGAkUQjQwS9d73YjSHhaIxpUw"))

      verify(playlistServiceMock).authenticate("access-code")
    }
  }
}

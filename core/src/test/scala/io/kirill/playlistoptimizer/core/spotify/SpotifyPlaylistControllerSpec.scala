package io.kirill.playlistoptimizer.core.spotify

import cats.effect.{ContextShift, IO}
import io.kirill.playlistoptimizer.core.common.errors.AuthenticationRequiredError
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.ControllerSpec
import io.kirill.playlistoptimizer.core.common.{SpotifyConfigBuilder}
import io.kirill.playlistoptimizer.core.common.json._
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.controllers.AppController.ErrorResponse
import io.kirill.playlistoptimizer.core.playlist.PlaylistOptimizer
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub

class SpotifyPlaylistControllerSpec extends ControllerSpec {

  implicit val sc: SpotifyConfig = SpotifyConfigBuilder.testConfig

  "A SpotifyPlaylistController" should {

    "return error when not authenticated" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case _ => throw new RuntimeException()
        }

      val playlistServiceMock = mock[SpotifyPlaylistService[IO]]
      val playlistOptimizerMock = mock[PlaylistOptimizer[IO]]
      val controller = new SpotifyPlaylistController(playlistOptimizerMock, playlistServiceMock, sc)

      when(playlistServiceMock.getAll).thenReturn(IO.raiseError(AuthenticationRequiredError("authorization with Spotify is required")));

      val request = Request[IO](uri = uri"/playlists")
      val response: IO[Response[IO]] = controller.routes.orNotFound.run(request)

      verifyResponse[ErrorResponse](response, Status.Forbidden, Some(ErrorResponse("authorization with Spotify is required")))
    }
  }
}

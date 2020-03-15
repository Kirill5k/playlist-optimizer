package io.kirill.playlistoptimizer.spotify

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import io.kirill.playlistoptimizer.common.configs.{SpotifyConfig, SpotifyConfigBuilder}
import io.kirill.playlistoptimizer.common.errors.ApplicationError.AuthenticationRequiredError
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.common.controllers.AppController.ErrorResponse
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub

import scala.concurrent.ExecutionContext

class SpotifyPlaylistControllerSpec extends AnyWordSpec with MockitoSugar with ArgumentMatchersSugar with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)
  implicit val sc: SpotifyConfig = SpotifyConfigBuilder.testConfig

  implicit val errorDec: EntityDecoder[IO, ErrorResponse] = jsonOf[IO, ErrorResponse]

  "A SpotifyPlaylistController" should {

    "return error when not authenticated" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case _ => throw new RuntimeException()
        }

      val playlistServiceMock = mock[SpotifyPlaylistService]
      val controller = new SpotifyPlaylistController() {
        override def playlistService: SpotifyPlaylistService = playlistServiceMock
      }

      when(playlistServiceMock.getAll).thenReturn(IO.raiseError(AuthenticationRequiredError("authorization with Spotify is required")));

      val request = Request[IO](uri = uri"/playlists")
      val response: IO[Response[IO]] = controller.routes.orNotFound.run(request)

      verifyResponse[ErrorResponse](response, Status.Forbidden, Some(ErrorResponse("authorization with Spotify is required")))
    }
  }

  def verifyResponse[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A] = None)(implicit ev: EntityDecoder[IO, A]): Unit = {
    val actualResp = actual.unsafeRunSync

    actualResp.status must be (expectedStatus)
    expectedBody match {
      case Some(expected) => actualResp.as[A].unsafeRunSync must be (expected)
      case None => actualResp.body.compile.toVector.unsafeRunSync mustBe empty
    }
  }
}

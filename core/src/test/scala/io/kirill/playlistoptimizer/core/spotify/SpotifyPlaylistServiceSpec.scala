package io.kirill.playlistoptimizer.core.spotify

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import io.kirill.playlistoptimizer.common.configs.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.configs.{SpotifyConfig, SpotifyConfigBuilder}
import io.kirill.playlistoptimizer.core.common.errors.ApplicationError.AuthenticationRequiredError
import io.kirill.playlistoptimizer.core.optimizer.Optimizer
import io.kirill.playlistoptimizer.core.playlist.Track
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub

import scala.concurrent.ExecutionContext

class SpotifyPlaylistServiceSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with MockitoSugar with ArgumentMatchersSugar {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)
  implicit val sc: SpotifyConfig = SpotifyConfigBuilder.testConfig

  "A SpotifyPlaylistService" - {

    "return error when not authenticated" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case _ => throw new RuntimeException()
        }

      val optimizerMock = mock[Optimizer[IO, Track]]
      val service = new SpotifyPlaylistService(optimizerMock)

      service.findByName("foo").assertThrows[AuthenticationRequiredError]
    }
  }
}

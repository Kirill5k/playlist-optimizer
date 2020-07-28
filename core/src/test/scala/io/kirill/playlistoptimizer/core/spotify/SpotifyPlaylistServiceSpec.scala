package io.kirill.playlistoptimizer.core.spotify

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.kirill.playlistoptimizer.core.common.SpotifyConfigBuilder
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.errors.AuthenticationRequiredError
import io.kirill.playlistoptimizer.core.playlist.{PlaylistOptimizer, Track}
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub

import scala.concurrent.ExecutionContext

class SpotifyPlaylistServiceSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with MockitoSugar with ArgumentMatchersSugar {
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)
  implicit val sc: SpotifyConfig = SpotifyConfigBuilder.testConfig

  "A SpotifyPlaylistService" - {

    "return error when not authenticated" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case _ => throw new RuntimeException()
        }

      val service = new SpotifyPlaylistService[IO]()

      service.findByName("foo").assertThrows[AuthenticationRequiredError]
    }
  }
}

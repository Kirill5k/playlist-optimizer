package io.kirill.playlistoptimizer.spotify

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import io.kirill.playlistoptimizer.common.configs.{SpotifyApiConfig, SpotifyAuthConfig, SpotifyConfig}
import io.kirill.playlistoptimizer.common.errors.ApplicationError.AuthenticationRequiredError
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub

import scala.concurrent.ExecutionContext

class SpotifyPlaylistServiceSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  val authConfig = SpotifyAuthConfig("http://account.spotify.com", "/authorize", "/token", "client-id", "client-secret", "/redirect")
  val apiConfig = SpotifyApiConfig("http://api.spotify.com", "/me", "/users", "/playlists", "/audio-analysis", "/audio-features")
  implicit val spotifyConfig = SpotifyConfig(authConfig, apiConfig)

  "A SpotifyPlaylistService" - {

    "return error when not authenticated" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case _ => throw new RuntimeException()
        }

      val service = new SpotifyPlaylistService()

      service.findByName("foo").assertThrows[AuthenticationRequiredError]
    }
  }
}

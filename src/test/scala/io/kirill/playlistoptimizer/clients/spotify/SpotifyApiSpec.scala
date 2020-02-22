package io.kirill.playlistoptimizer.clients.spotify

import java.io.File

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import cats.implicits._
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.Method

import scala.concurrent.ExecutionContext

class SpotifyApiSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  val spotifyConfig = SpotifyConfig("http://spotify.com", "/auth", "client-id", "client-secret")

  val authSuccessResponseJson = new File("spotify/auth-success-response.json")
  val authErrorResponseJson = new File("spotify/auth-error-response.json")

  "A SpotifyApi" - {
    "return auth token" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatches(_.uri.path.startsWith(List("a", "b")))
        .thenRespond("Hello there!")
        .whenRequestMatches(_.method == Method.POST)
        .thenRespond(authSuccessResponseJson)

      val authResponse = SpotifyApi.authenticate[IO](spotifyConfig)
      authResponse.asserting(_ must be (1))
    }
  }
}

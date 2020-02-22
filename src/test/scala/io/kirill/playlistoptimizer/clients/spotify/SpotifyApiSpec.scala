package io.kirill.playlistoptimizer.clients.spotify

import java.io.File

import cats.effect.{ContextShift, IO}
import cats.implicits._
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import org.scalatest.wordspec.AnyWordSpec
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.Method

import scala.concurrent.ExecutionContext

class SpotifyApiSpec extends AnyWordSpec {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  val spotifyConfig = SpotifyConfig("http://spotify.com", "/auth", "client-id", "client-secret")

  val authSuccessResponseJson = new File("spotify/auth-success-response.json")
  val authErrorResponseJson = new File("spotify/auth-error-response.json")

  "A SpotifyApi" should {
    "return auth token" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatches(_.uri.path.startsWith(List("a", "b")))
        .thenRespond("Hello there!")
        .whenRequestMatches(_.method == Method.POST)
        .thenRespond(authSuccessResponseJson)

      val authResponse = SpotifyApi.authenticate[IO](spotifyConfig)
    }
  }
}

package io.kirill.playlistoptimizer.clients.spotify

import java.io.File

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import cats.implicits._
import io.kirill.playlistoptimizer.clients.spotify.SpotifyResponse.SpotifyAuthResponse
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.Method

import scala.concurrent.ExecutionContext
import scala.io.Source

class SpotifyApiSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  implicit val spotifyConfig = SpotifyConfig("http://spotify.com", "/auth", "client-id", "client-secret")

  val authSuccessResponseJson = Source.fromResource("spotify/auth-success-response.json").getLines.toList.mkString
  val authErrorResponseJson = Source.fromResource("spotify/auth-error-response.json").getLines.toList.mkString

  "A SpotifyApi" - {
    "return auth response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatches(req => req.uri.hostSegment.v == "spotify.com/auth" && req.method == Method.POST)
        .thenRespond(authSuccessResponseJson)

      val authResponse = SpotifyApi.authenticate[IO]

      authResponse.asserting(_ must be (SpotifyAuthResponse("BQC3wD_w-ODtKQsbz7woOZPvffQX5iX7rychivVGQxO3qzgejLCgXwAE5acsqk8LQcih2qpDkaCjrJRRhuY", "Bearer", 3600, "")))
    }
  }
}

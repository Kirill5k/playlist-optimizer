package io.kirill.playlistoptimizer.clients.spotify

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import io.circe.ParsingFailure
import io.kirill.playlistoptimizer.clients.spotify.SpotifyError._
import io.kirill.playlistoptimizer.clients.spotify.SpotifyResponse._
import io.kirill.playlistoptimizer.configs.{SpotifyApiConfig, SpotifyAuthConfig, SpotifyConfig}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client
import sttp.client.Response
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.{Header, Method, StatusCode}

import scala.concurrent.ExecutionContext
import scala.io.Source

class SpotifyAuthApiSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  val authConfig = SpotifyAuthConfig("http://account.spotify.com", "/auth", "client-id", "client-secret", "user-id")
  val apiConfig = SpotifyApiConfig("http://api.spotify.com", "/users", "/playlists", "/audio-analysis", "/audio-features")
  implicit val spotifyConfig = SpotifyConfig(authConfig, apiConfig)

  "A SpotifyAuthApi" - {

    "return auth response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "account.spotify.com/auth" && r.method == Method.POST =>
            Response.ok(json("spotify/api/auth-response.json"))
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyAuthApi.authenticateClient[IO]

      authResponse.asserting(_ must be(SpotifyAuthResponse("BQC3wD_w-ODtKQsbz7woOZPvffQX5iX7rychivVGQxO3qzgejLCgXwAE5acsqk8LQcih2qpDkaCjrJRRhuY", "Bearer", 3600, "")))
    }

    "return auth error when failure" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "account.spotify.com/auth" && r.method == Method.POST =>
            Response(json("spotify/api/auth-error.json"), StatusCode.InternalServerError)
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyAuthApi.authenticateClient[IO]

      authResponse.assertThrows[SpotifyAuthError]
    }

  }

  def json(path: String): String = Source.fromResource(path).getLines.toList.mkString
}

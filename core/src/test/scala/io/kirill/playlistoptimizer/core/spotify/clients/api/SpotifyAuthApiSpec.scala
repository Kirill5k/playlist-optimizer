package io.kirill.playlistoptimizer.core.spotify.clients.api

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import io.kirill.playlistoptimizer.common.configs.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.configs.SpotifyConfigBuilder
import SpotifyError.SpotifyAuthError
import SpotifyResponse.{SpotifyAuthRefreshResponse, SpotifyAuthResponse}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client.Response
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.{Method, StatusCode}

import scala.concurrent.ExecutionContext
import scala.io.Source

class SpotifyAuthApiSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)
  implicit val sc = SpotifyConfigBuilder.testConfig

  "A SpotifyAuthApi" - {

    "return auth response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "account.spotify.com" && r.uri.path == List("api", "token") && r.method == Method.POST && r.body.toString.contains("grant_type=authorization_code&code=code&redirect_uri=%2Fredirect") =>
            Response.ok(json("spotify/api/auth-response.json"))
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyAuthApi.authorize[IO]("code")

      authResponse.asserting(_ must be(SpotifyAuthResponse("BQC3wD_w-ODtKQsbz7woOZPvffQX5iX7rychivVGQxO3qzgejLCgXwAE5acsqk8LQcih2qpDkaCjrJRRhuY", "Bearer", 3600, "")))
    }

    "return auth refresh response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "account.spotify.com" && r.uri.path == List("api", "token") && r.method == Method.POST && r.body.toString.contains("refresh_token=token") =>
            Response.ok(json("spotify/api/auth-refresh-response.json"))
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyAuthApi.refresh[IO]("token")

      authResponse.asserting(_ must be(SpotifyAuthRefreshResponse("NgA6ZcYI...ixn8bUQ", "Bearer", 3600, "user-read-private user-read-email")))
    }

    "return auth error when failure" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "account.spotify.com" && r.uri.path == List("api", "token") && r.method == Method.POST =>
            Response(json("spotify/api/auth-error.json"), StatusCode.InternalServerError)
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyAuthApi.authorize[IO]("code")

      authResponse.assertThrows[SpotifyAuthError]
    }

  }

  def json(path: String): String = Source.fromResource(path).getLines.toList.mkString
}

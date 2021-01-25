package io.kirill.playlistoptimizer.core.spotify.clients.api

import cats.effect.IO
import io.kirill.playlistoptimizer.core.ApiClientSpec
import io.kirill.playlistoptimizer.core.common.errors.SpotifyApiError
import io.kirill.playlistoptimizer.core.spotify.clients.api.SpotifyResponse.{SpotifyAuthRefreshResponse, SpotifyAuthResponse}
import sttp.client3.{Response, SttpBackend}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.model.{Method, StatusCode}

class SpotifyAuthApiSpec extends ApiClientSpec {

  "A SpotifyAuthApi" - {

    "return auth response when success" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host.contains("account.spotify.com") && r.uri.path == List("api", "token") && r.method == Method.POST && r.body.toString.contains("grant_type=authorization_code&code=code&redirect_uri=%2Fredirect") =>
            Response.ok(json("spotify/api/auth-response.json"))
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyAuthApi.authorize[IO]("code")

      authResponse.asserting(_ must be(SpotifyAuthResponse("BQC3wD_w-ODtKQsbz7woOZPvffQX5iX7rychivVGQxO3qzgejLCgXwAE5acsqk8LQcih2qpDkaCjrJRRhuY", "Bearer", 3600, "", "cnczbmrInWjs4So1F4Gm")))
    }

    "return auth refresh response when success" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host.contains("account.spotify.com") && r.uri.path == List("api", "token") && r.method == Method.POST && r.body.toString.contains("refresh_token=token&grant_type=refresh_token") =>
            Response.ok(json("spotify/api/auth-refresh-response.json"))
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyAuthApi.refresh[IO]("token")

      authResponse.asserting { res =>
        res mustBe SpotifyAuthRefreshResponse("NgA6ZcYI...ixn8bUQ", "Bearer", 3600, "user-read-private user-read-email")
      }
    }

    "return auth error when failure" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host.contains("account.spotify.com") && r.uri.path == List("api", "token") && r.method == Method.POST =>
            Response(json("spotify/api/auth-error.json"), StatusCode.InternalServerError)
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyAuthApi.authorize[IO]("code")

      authResponse.attempt.map { res =>
        res mustBe Left(SpotifyApiError("Invalid client secret"))
      }
    }

  }
}

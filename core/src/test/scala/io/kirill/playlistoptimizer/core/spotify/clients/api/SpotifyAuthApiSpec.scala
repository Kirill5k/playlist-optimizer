package io.kirill.playlistoptimizer.core.spotify.clients.api

import cats.effect.IO
import io.kirill.playlistoptimizer.core.ApiClientSpec
import io.kirill.playlistoptimizer.domain.errors.SpotifyApiError
import io.kirill.playlistoptimizer.core.spotify.clients.api.responses.{SpotifyAuthRefreshResponse, SpotifyAuthResponse}
import sttp.client3.{Response, SttpBackend}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.model.StatusCode

class SpotifyAuthApiSpec extends ApiClientSpec {

  "A SpotifyAuthApi" - {

    "return auth response when success" in {
      given testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.isGoingTo("account.spotify.com/api/token") && r.isPost && r.bodyContains("grant_type=authorization_code&code=code&redirect_uri=%2Fredirect") =>
            Response.ok(json("spotify/api/auth-response.json"))
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyAuthApi.authorize[IO]("code")

      authResponse.asserting { res =>
        res mustBe SpotifyAuthResponse("BQC3wD_w-ODtKQsbz7woOZPvffQX5iX7rychivVGQxO3qzgejLCgXwAE5acsqk8LQcih2qpDkaCjrJRRhuY", "Bearer", 3600, "", "cnczbmrInWjs4So1F4Gm")
      }
    }

    "return auth refresh response when success" in {
      given testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.isGoingTo("account.spotify.com/api/token") && r.isPost && r.bodyContains("refresh_token=token&grant_type=refresh_token") =>
            Response.ok(json("spotify/api/auth-refresh-response.json"))
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyAuthApi.refresh[IO]("token")

      authResponse.asserting { res =>
        res mustBe SpotifyAuthRefreshResponse("NgA6ZcYI...ixn8bUQ", "Bearer", 3600, "user-read-private user-read-email")
      }
    }

    "return auth error when failure" in {
      given testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.isGoingTo("account.spotify.com/api/token") && r.isPost =>
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

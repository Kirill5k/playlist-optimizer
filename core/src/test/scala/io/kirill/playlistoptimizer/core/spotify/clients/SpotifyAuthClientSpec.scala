package io.kirill.playlistoptimizer.core.spotify.clients

import cats.effect.IO
import io.kirill.playlistoptimizer.core.ApiClientSpec
import io.kirill.playlistoptimizer.core.RequestOps._
import io.kirill.playlistoptimizer.core.spotify.SpotifyAccessToken
import sttp.client3.{Response, SttpBackend}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend


class SpotifyAuthClientSpec extends ApiClientSpec {

  "A SpotifyAuthClient" - {

    "obtain authorization code" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.isGoingTo("account.spotify.com/api/token") && r.isPost && r.bodyContains("grant_type=authorization_code&code=code&redirect_uri=%2Fredirect") =>
            Response.ok(json("spotify/flow/auth/1-auth.json"))
          case r if r.isGoingTo("api.spotify.com/v1/me") && r.isGet =>
            Response.ok(json("spotify/flow/auth/2-current-user.json"))
          case r =>
            throw new RuntimeException(s"no mocks for ${r.uri.host.get}/${r.uri.path.mkString("/")}")
        }

      val client = new LiveSpotifyAuthClient[IO]()

      client
        .authorize("code")
        .asserting { t =>
          t.accessToken mustBe "access-O3qzgejLCgXwAE5acsqk8LQcih2qpDkaCjrJRRhuY"
          t.refreshToken mustBe "cnczbmrInWjs4So1F4Gm"
        }
    }

    "obtain new refreshed token when original token has expired" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.isGoingTo("account.spotify.com/api/token") && r.isPost && r.body.toString.contains("refresh_token=code") =>
            Response.ok(json("spotify/flow/auth/4-refreshed.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host.get}/${r.uri.path.mkString("/")}")
        }

      val accessToken = SpotifyAccessToken("expired-token", "code", "user-id", 0)
      val client = new LiveSpotifyAuthClient[IO]()


      client
        .refresh(accessToken)
        .asserting(_.accessToken mustBe "refresh-O3qzgejLCgXwAE5acsqk8LQcih2qpDkaCjrJRRhuY")
    }
  }
}

package io.kirill.playlistoptimizer.core.spotify.clients

import cats.effect.{ContextShift, IO}
import cats.effect.testing.scalatest.AsyncIOSpec
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.kirill.playlistoptimizer.core.ApiClientSpec
import io.kirill.playlistoptimizer.core.common.SpotifyConfigBuilder
import io.kirill.playlistoptimizer.core.common.errors._
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.spotify.SpotifyAccessToken
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client.Response
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.Method

import scala.io.Source

class SpotifyAuthClientSpec extends ApiClientSpec {

  "A SpotifyAuthClient" - {

    "obtain authorization code" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "account.spotify.com" && r.uri.path == List("api", "token") && r.method == Method.POST && r.body.toString.contains("grant_type=authorization_code&code=code&redirect_uri=%2Fredirect") =>
            Response.ok(json("spotify/flow/auth/1-auth.json"))
          case r if r.uri.host == "api.spotify.com" && r.uri.path == List("v1", "me") && r.method == Method.GET =>
            Response.ok(json("spotify/flow/auth/2-current-user.json"))
          case r =>
            throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val client = new SpotifyAuthClient[IO]()

      client
        .authorize("code")
        .asserting { t =>
          t.accessToken must be ("access-O3qzgejLCgXwAE5acsqk8LQcih2qpDkaCjrJRRhuY")
          t.refreshToken must be ("cnczbmrInWjs4So1F4Gm")
        }
    }

    "obtain new refreshed token when original token has expired" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "account.spotify.com" && r.uri.path == List("api", "token") && r.method == Method.POST && r.body.toString.contains("refresh_token=code") =>
            Response.ok(json("spotify/flow/auth/4-refreshed.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val accessToken = SpotifyAccessToken("expired-token", "code", "user-id", 0)
      val client = new SpotifyAuthClient[IO]()


      client
        .refresh(accessToken)
        .asserting(_.accessToken must be ("refresh-O3qzgejLCgXwAE5acsqk8LQcih2qpDkaCjrJRRhuY"))
    }
  }
}

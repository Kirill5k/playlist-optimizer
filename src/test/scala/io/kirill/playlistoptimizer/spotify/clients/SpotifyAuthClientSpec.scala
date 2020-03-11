package io.kirill.playlistoptimizer.spotify.clients

import cats.effect.{ContextShift, IO}
import cats.effect.testing.scalatest.AsyncIOSpec
import io.kirill.playlistoptimizer.configs.{SpotifyConfig, SpotifyConfigBuilder}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sttp.client.Response
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.Method

import scala.concurrent.ExecutionContext
import scala.io.Source

class SpotifyAuthClientSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)
  implicit val sc: SpotifyConfig = SpotifyConfigBuilder.testConfig

  "A SpotifyAuthClient" - {

    "send authorization and get current requests on initialization" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "account.spotify.com/token" && r.method == Method.POST && r.body.toString.contains("grant_type=authorization_code&code=code&redirect_uri=%2Fredirect") =>
            Response.ok(json("spotify/flow/auth/1-auth.json"))
          case r if r.uri.host == "api.spotify.com/me" && r.method == Method.GET =>
            Response.ok(json("spotify/flow/auth/2-current-user.json"))
          case _ => throw new RuntimeException()
        }

      val client = new SpotifyAuthClient("code")

      client.token.asserting(_ must be ("access-O3qzgejLCgXwAE5acsqk8LQcih2qpDkaCjrJRRhuY"))
      client.userId.asserting(_ must be ("wizzler"))
    }

    "obtain new refreshed token when original token has expired" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "account.spotify.com/token" && r.method == Method.POST && r.body.toString.contains("grant_type=authorization_code&code=code&redirect_uri=%2Fredirect") =>
            Response.ok(json("spotify/flow/auth/3-auth-expired.json"))
          case r if r.uri.host == "api.spotify.com/me" && r.method == Method.GET =>
            Response.ok(json("spotify/flow/auth/2-current-user.json"))
          case r if r.uri.host == "account.spotify.com/token" && r.method == Method.POST && r.body.toString.contains("refresh_token=cnczbmrInWjs4So1F4Gm") =>
            Response.ok(json("spotify/flow/auth/4-refreshed.json"))
          case _ => throw new RuntimeException()
        }

      val client = new SpotifyAuthClient("code")

      client.token.asserting(_ must be ("refresh-O3qzgejLCgXwAE5acsqk8LQcih2qpDkaCjrJRRhuY"))
      client.userId.asserting(_ must be ("wizzler"))
    }
  }

  def json(path: String): String = Source.fromResource(path).getLines.toList.mkString
}
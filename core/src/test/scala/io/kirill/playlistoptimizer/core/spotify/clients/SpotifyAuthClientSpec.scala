package io.kirill.playlistoptimizer.core.spotify.clients

import cats.effect.{ContextShift, IO}
import cats.effect.testing.scalatest.AsyncIOSpec
import io.kirill.playlistoptimizer.common.configs.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.errors.ApplicationError._
import io.kirill.playlistoptimizer.core.common.configs.{SpotifyConfig, SpotifyConfigBuilder}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
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

    "return error when not authorized" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case _ => throw new RuntimeException()
        }

      val client = new SpotifyAuthClient()

      client.token.assertThrows[AuthenticationRequiredError]
    }

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

      val client = new SpotifyAuthClient()

      client.authorize("code").flatMap(_ => client.token.asserting(_ must be ("access-O3qzgejLCgXwAE5acsqk8LQcih2qpDkaCjrJRRhuY")))
    }

    "obtain new refreshed token when original token has expired" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "account.spotify.com" && r.uri.path == List("api", "token") && r.method == Method.POST && r.body.toString.contains("grant_type=authorization_code&code=code&redirect_uri=%2Fredirect") =>
            Response.ok(json("spotify/flow/auth/3-auth-expired.json"))
          case r if r.uri.host == "api.spotify.com" && r.uri.path == List("v1", "me") && r.method == Method.GET =>
            Response.ok(json("spotify/flow/auth/2-current-user.json"))
          case r if r.uri.host == "account.spotify.com" && r.uri.path == List("api", "token") && r.method == Method.POST && r.body.toString.contains("refresh_token=code") =>
            Response.ok(json("spotify/flow/auth/4-refreshed.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val client = new SpotifyAuthClient


      client.authorize("code").flatMap(_ => client.token.asserting(_ must be ("refresh-O3qzgejLCgXwAE5acsqk8LQcih2qpDkaCjrJRRhuY")))
    }
  }

  def json(path: String): String = Source.fromResource(path).getLines.toList.mkString
}

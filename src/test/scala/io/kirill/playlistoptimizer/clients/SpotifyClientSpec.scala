package io.kirill.playlistoptimizer.clients

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import io.kirill.playlistoptimizer.configs.{SpotifyApiConfig, SpotifyAuthConfig, SpotifyConfig}
import io.kirill.playlistoptimizer.domain.Key._
import io.kirill.playlistoptimizer.domain._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client
import sttp.client.Response
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.Header

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps

class SpotifyClientSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  val authConfig = SpotifyAuthConfig("http://account.spotify.com", "/auth", "client-id", "client-secret", "user-1")
  val apiConfig = SpotifyApiConfig("http://api.spotify.com", "/users", "/playlists", "/audio-analysis", "/audio-features")
  implicit val spotifyConfig = SpotifyConfig(authConfig, apiConfig)

  "A SpotifyClient" - {

    "find playlist by name" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "account.spotify.com/auth" => Response.ok(json("spotify/flow/1-auth.json"))
          case r if isAuthorized(r, "api.spotify.com/users", List("user-1", "playlists")) => Response.ok(json("spotify/flow/2-users-playlists.json"))
          case r if isAuthorized(r, "api.spotify.com/playlists", List("7npAZEYwEwV2JV7XX2n3wq")) => Response.ok(json("spotify/flow/3-playlist.json"))
          case r if isAuthorized(r, "api.spotify.com/audio-features") => Response.ok(json("spotify/flow/4-audio-features-2aJDlirz6v2a4HREki98cP.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = ApiClient.spotifyClient.findPlaylistByName("mel")

      response.asserting(_ must be(Playlist("Mel", Some("Melodic deep house and techno songs"), PlaylistSource.Spotify, Vector(
        Track(SongDetails("Glue", List("Bicep"), Some("Bicep")), AudioDetails(129.983, 269149930 microseconds, CMinor)),
        Track(SongDetails("In Heaven", List("Dustin Nantais", "Paul Hazendonk"), Some("Novel Creations, Vol. 1")), AudioDetails(123.018, 411773060 microseconds, FSharpMajor))
      ))))
    }
  }

  def isAuthorized(req: client.Request[_, _], host: String, paths: Seq[String] = Nil): Boolean =
    req.uri.host == host && (paths.isEmpty || req.uri.path == paths) &&
      req.headers.contains(new Header("Authorization", "Bearer BQCK-13bJ_7Qp6sa8DPvNBtvviUDasacL___qpx88zl6M2GDFjnL7qzG9WB9j7DtXmGrLML2Dy1DGPutRPabx316KIskN0amIZmdBZd7EKs3kFA1eXyu5HsjmwdHRD5lcpIsBqfb0Slx9fzZuCu_rM3aBDg"))

  def json(path: String): String = Source.fromResource(path).getLines.toList.mkString
}

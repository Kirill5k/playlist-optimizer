package io.kirill.playlistoptimizer.clients

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import io.kirill.playlistoptimizer.configs.{SpotifyApiConfig, SpotifyAuthConfig, SpotifyConfig}
import io.kirill.playlistoptimizer.domain.Key._
import io.kirill.playlistoptimizer.domain._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client.Response
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps

class SpotifyClientSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  val authConfig = SpotifyAuthConfig("http://account.spotify.com", "/auth", "client-id", "client-secret", "user-1")
  val apiConfig = SpotifyApiConfig("http://api.spotify.com", "/users", "/playlists", "/audio-analysis")
  implicit val spotifyConfig = SpotifyConfig(authConfig, apiConfig)

  val authResponseJson = Source.fromResource("spotify/flow/1-auth.json").getLines.toList.mkString
  val usersPlaylistResponseJson = Source.fromResource("spotify/flow/2-users-playlists.json").getLines.toList.mkString
  val playlistResponseJson = Source.fromResource("spotify/flow/3-playlist.json").getLines.toList.mkString
  val audioAnalysis1ResponseJson = Source.fromResource("spotify/flow/4-audio-analysis-1.json").getLines.toList.mkString
  val audioAnalysis2ResponseJson = Source.fromResource("spotify/flow/5-audio-analysis-2.json").getLines.toList.mkString

  "A SpotifyClient" - {

    "find playlist by name" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "account.spotify.com/auth" => Response.ok(authResponseJson)
          case r if r.uri.host == "api.spotify.com/users" && r.uri.path == List("user-1", "playlists") => Response.ok(usersPlaylistResponseJson)
          case r if r.uri.host == "api.spotify.com/playlists" && r.uri.path == List("7npAZEYwEwV2JV7XX2n3wq") => Response.ok(playlistResponseJson)
          case r if r.uri.host == "api.spotify.com/audio-analysis" && r.uri.path == List("2aJDlirz6v2a4HREki98cP") => Response.ok(audioAnalysis1ResponseJson)
          case r if r.uri.host == "api.spotify.com/audio-analysis" && r.uri.path == List("6AjUFYqP7oVTUX47cVJins") => Response.ok(audioAnalysis2ResponseJson)
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = ApiClient.spotifyClient.findPlaylistByName("mel")

      response.asserting(_ must be(Playlist("Mel", Some("Melodic deep house and techno songs"), PlaylistSource.Spotify, Vector(
        Track(SongDetails("Glue", List("Bicep"), Some("Bicep")), AudioDetails(129.983, 269149930 microseconds, CMinor)),
        Track(SongDetails("In Heaven", List("Dustin Nantais", "Paul Hazendonk"), Some("Novel Creations, Vol. 1")), AudioDetails(123.018, 411773060 microseconds, FSharpMajor))
      ))))
    }
  }
}

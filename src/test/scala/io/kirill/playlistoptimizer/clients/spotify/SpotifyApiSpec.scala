package io.kirill.playlistoptimizer.clients.spotify

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import cats.implicits._
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

class SpotifyApiSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  val authConfig = SpotifyAuthConfig("http://account.spotify.com", "/auth", "client-id", "client-secret", "user-id")
  val apiConfig = SpotifyApiConfig("http://api.spotify.com", "/users", "/playlists", "/audio-analysis", "/audio-features")
  implicit val spotifyConfig = SpotifyConfig(authConfig, apiConfig)

  val authSuccessResponseJson = Source.fromResource("spotify/api/auth-response.json").getLines.toList.mkString
  val audioAnalysisResponseJson = Source.fromResource("spotify/api/audio-analysis-response.json").getLines.toList.mkString
  val playlistResponseJson = Source.fromResource("spotify/api/playlist-response.json").getLines.toList.mkString
  val playlistsResponseJson = Source.fromResource("spotify/api/playlists-response.json").getLines.toList.mkString
  val authErrorResponseJson = Source.fromResource("spotify/api/auth-error.json").getLines.toList.mkString

  "A SpotifyApi" - {
    "return auth response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "account.spotify.com/auth" && r.method == Method.POST => Response.ok(authSuccessResponseJson)
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyApi.authenticate[IO]

      authResponse.asserting(_ must be (SpotifyAuthResponse("BQC3wD_w-ODtKQsbz7woOZPvffQX5iX7rychivVGQxO3qzgejLCgXwAE5acsqk8LQcih2qpDkaCjrJRRhuY", "Bearer", 3600, "")))
    }

    "return auth error when failure" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "account.spotify.com/auth" && r.method == Method.POST => Response(authErrorResponseJson, StatusCode.InternalServerError)
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyApi.authenticate[IO]

      authResponse.assertThrows[SpotifyAuthError]
    }

    "return audio analysis response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com/audio-analysis", List("track-1")) => Response.ok(audioAnalysisResponseJson)
          case _ => throw new RuntimeException()
        }

      val response = SpotifyApi.getAudioAnalysis[IO]("token", "track-1")

      response.asserting(_ must be (SpotifyAudioAnalysisResponse(AudioAnalysisTrack(255.34898, 98.002, 5, 0))))
    }

    "return audio features response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com/audio-features", List("track-1")) => Response.ok(json("spotify/api/audio-features-response.json"))
          case _ => throw new RuntimeException()
        }

      val response = SpotifyApi.getAudioFeatures[IO]("token", "track-1")

      response.asserting(_ must be (SpotifyAudioFeaturesResponse(7,0,535975.0,123.996)))
    }

    "return playlist response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com/playlists", List("playlist-1")) => Response.ok(playlistResponseJson)
          case _ => throw new RuntimeException()
        }

      val response = SpotifyApi.getPlaylist[IO]("token", "playlist-1")

      response.asserting(_ must be (SpotifyPlaylistResponse(
        "59ZbFPES4DQwEjBpWHzrtC",
        "Dinner with Friends",
        Some("Having friends over for dinner? Here´s the perfect playlist."),
        PlaylistTracks(Vector(PlaylistItem(PlaylistTrack("4i9sYtSIlR80bxje5B3rUb", "I'm Not The Only One - Radio Edit", PlaylistTrackAlbum("5GWoXPsTQylMuaZ84PC563", "single", "I'm Not The Only One"), List(PlaylistTrackArtist("2wY79sveU1sp5g7SokKOiI", "Sam Smith")),45.0))),105)
      )))
    }

    "return playlists response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com/users", List("user-1", "playlists")) => Response.ok(playlistsResponseJson)
          case _ => throw new RuntimeException()
        }

      val response = SpotifyApi.getUserPlaylists[IO]("token", "user-1")

      response.asserting(_ must be (SpotifyPlaylistsResponse(List(
        PlaylistsItem("53Y8wT46QIMz5H4WQ8O22c", "Wizzlers Big Playlist"),
        PlaylistsItem("1AVZz0mBuGbCEoNRQdYQju", "Another Playlist")),9)))
    }

    "return error when corrupted json" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com/users")  => Response.ok("""{"foo"}""")
          case _ => throw new RuntimeException()
        }

      val response = SpotifyApi.getUserPlaylists[IO]("token", "user-1")

      response.assertThrows[ParsingFailure]
    }
  }

  def isAuthorized(req: client.Request[_, _], host: String, paths: Seq[String] = Nil, token: String = "token"): Boolean =
    req.uri.host == host && (paths.isEmpty || req.uri.path == paths) &&
      req.headers.contains(new Header("Authorization", s"Bearer $token"))

  def json(path: String): String = Source.fromResource(path).getLines.toList.mkString
}

package io.kirill.playlistoptimizer.spotify.clients.api

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import io.circe.ParsingFailure
import io.kirill.playlistoptimizer.configs.{SpotifyApiConfig, SpotifyAuthConfig, SpotifyConfig, SpotifyConfigBuilder}
import io.kirill.playlistoptimizer.spotify.clients.api.SpotifyResponse._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client
import sttp.client.Response
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.{Header, Method, StatusCode}

import scala.concurrent.ExecutionContext
import scala.io.Source

class SpotifyRestApiSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)
  implicit val sc = SpotifyConfigBuilder.testConfig

  "A SpotifyRestApi" - {

    "return current user when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com/me") =>
            Response.ok(json("spotify/api/user-response.json"))
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.getCurrentUser[IO]("token")

      response.asserting(_ must be (SpotifyUserResponse("wizzler", "JM Wizzler", "email@example.com")))
    }

    "return audio analysis response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com/audio-analysis", List("track-1")) =>
            Response.ok(json("spotify/api/audio-analysis-response.json"))
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.getAudioAnalysis[IO]("token", "track-1")

      response.asserting(_ must be (SpotifyAudioAnalysisResponse(AudioAnalysisTrack(255.34898, 98.002, 5, 0))))
    }

    "return audio features response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com/audio-features", List("track-1")) =>
            Response.ok(json("spotify/api/audio-features-response.json"))
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.getAudioFeatures[IO]("token", "track-1")

      response.asserting(_ must be (SpotifyAudioFeaturesResponse(7,0,535975.0,123.996)))
    }

    "return playlist response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com/playlists", List("playlist-1")) =>
            Response.ok(json("spotify/api/playlist-response.json"))
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.getPlaylist[IO]("token", "playlist-1")

      response.asserting(_ must be (SpotifyPlaylistResponse(
        "59ZbFPES4DQwEjBpWHzrtC",
        "Dinner with Friends",
        Some("Having friends over for dinner? Here´s the perfect playlist."),
        PlaylistTracks(Vector(PlaylistItem(PlaylistTrack("4i9sYtSIlR80bxje5B3rUb", "I'm Not The Only One - Radio Edit", PlaylistTrackAlbum("5GWoXPsTQylMuaZ84PC563", "single", "I'm Not The Only One", Some("2012-10-10"), Some("day")), List(PlaylistTrackArtist("2wY79sveU1sp5g7SokKOiI", "Sam Smith")),45.0, "spotify:track:4i9sYtSIlR80bxje5B3rUb", PlaylistTrackUrls("https://open.spotify.com/track/4i9sYtSIlR80bxje5B3rUb")))),105)
      )))
    }

    "return user playlists response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com/users", List("user-1", "playlists")) =>
            Response.ok(json("spotify/api/playlists-response.json"))
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.getUserPlaylists[IO]("token", "user-1")

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

      val response = SpotifyRestApi.getUserPlaylists[IO]("token", "user-1")

      response.assertThrows[ParsingFailure]
    }

    "create playlist for a user when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com/users", List("user-1", "playlists")) && hasBody(r, """{"name":"my-playlist","description":"new-playlist-to-be-created","public":true,"collaborative":false}""") =>
            Response.ok(json("spotify/api/playlist-response.json"))
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.createPlaylist[IO]("token", "user-1", "my-playlist", Some("new-playlist-to-be-created"))

      response.asserting(_ must be (SpotifyPlaylistResponse(
        "59ZbFPES4DQwEjBpWHzrtC",
        "Dinner with Friends",
        Some("Having friends over for dinner? Here´s the perfect playlist."),
        PlaylistTracks(Vector(PlaylistItem(PlaylistTrack("4i9sYtSIlR80bxje5B3rUb", "I'm Not The Only One - Radio Edit", PlaylistTrackAlbum("5GWoXPsTQylMuaZ84PC563", "single", "I'm Not The Only One", Some("2012-10-10"), Some("day")), List(PlaylistTrackArtist("2wY79sveU1sp5g7SokKOiI", "Sam Smith")),45.0, "spotify:track:4i9sYtSIlR80bxje5B3rUb", PlaylistTrackUrls("https://open.spotify.com/track/4i9sYtSIlR80bxje5B3rUb")))),105)
      )))
    }

    "add tracks to a playlist" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com/playlists", List("playlist-1", "tracks")) && hasBody(r, """{"uris":["uri-1","uri-2","uri-3"],"position":null}""") =>
            Response(json("spotify/api/operation-success-response.json"), StatusCode.Created)
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.addTracksToPlaylist[IO]("token", "playlist-1", List("uri-1", "uri-2", "uri-3"))

      response.asserting(_ must be (SpotifyOperationSuccessResponse("JbtmHBDBAYu3/bt8BOXKjzKx3i0b6LCa/wVjyl6qQ2Yf6nFXkbmzuEa+ZI/U1yF+")))
    }

    "replace tracks in a playlist" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com/playlists", List("playlist-1", "tracks")) && hasBody(r, """{"uris":["uri-1","uri-2","uri-3"]}""", Method.PUT) =>
            Response(json("spotify/api/operation-success-response.json"), StatusCode.Created)
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.replaceTracksInPlaylist[IO]("token", "playlist-1", List("uri-1", "uri-2", "uri-3"))

      response.asserting(_ must be (()))
    }
  }

  def isAuthorized(req: client.Request[_, _], host: String, paths: Seq[String] = Nil, token: String = "token"): Boolean =
    req.uri.host == host && (paths.isEmpty || req.uri.path == paths) &&
      req.headers.contains(new Header("Authorization", s"Bearer $token"))

  def hasBody(req: client.Request[_, _], jsonBody: String, method: Method = Method.POST): Boolean =
    req.method == method && req.body.toString.contains(jsonBody)

  def json(path: String): String = Source.fromResource(path).getLines.toList.mkString
}

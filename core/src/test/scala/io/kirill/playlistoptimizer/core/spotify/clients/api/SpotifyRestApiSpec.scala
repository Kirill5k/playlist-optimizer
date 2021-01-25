package io.kirill.playlistoptimizer.core.spotify.clients.api

import cats.effect.IO
import io.kirill.playlistoptimizer.core.ApiClientSpec
import io.kirill.playlistoptimizer.core.common.errors.SpotifyApiError
import io.kirill.playlistoptimizer.core.spotify.clients.api.SpotifyResponse._
import sttp.client3
import sttp.client3.{Response, SttpBackend}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.model.{Header, Method, StatusCode}

class SpotifyRestApiSpec extends ApiClientSpec {

  "A SpotifyRestApi" - {

    "find track by name" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "search")) =>
            Response.ok(json("spotify/api/search-track-response.json"))
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.findTrack[IO]("token", "bicep glue")

      response.asserting { res =>
        res.tracks.total must be (6)
        res.tracks.items.size must be (1)
        val track = res.tracks.items.head
        track.uri must be ("spotify:track:2aJDlirz6v2a4HREki98cP")
        track.id must be ("2aJDlirz6v2a4HREki98cP")
      }
    }

    "return current user when success" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "me")) =>
            Response.ok(json("spotify/api/user-response.json"))
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.getCurrentUser[IO]("token")

      response.asserting(_ must be (SpotifyUserResponse("wizzler", "JM Wizzler")))
    }

    "return audio analysis response when success" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "audio-analysis", "track-1")) =>
            Response.ok(json("spotify/api/audio-analysis-response.json"))
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.getAudioAnalysis[IO]("token", "track-1")

      response.asserting(_ must be (SpotifyAudioAnalysisResponse(AudioAnalysisTrack(255.34898, 98.002, 5, 0))))
    }

    "return audio features response when success" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "audio-features", "track-1")) =>
            Response.ok(json("spotify/api/audio-features-response.json"))
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.getAudioFeatures[IO]("token", "track-1")

      response.asserting(_ must be (SpotifyAudioFeaturesResponse("1wtxI9YhL1t4yDIwGAFljP", 7,0,535975.0,123.996, 0.807, 0.613)))
    }

    "return multiple audio features response when success" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "audio-features")) =>
            Response.ok(json("spotify/api/multiple-audio-features-response.json"))
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.getMultipleAudioFeatures[IO]("token", List("track-1", "track-2"))

      response.asserting(_ must be (SpotifyMultipleAudioFeaturesResponse(List(
        SpotifyAudioFeaturesResponse("4JpKVNYnVcJ8tuMKjAj50A", 7,1,535223.0,123.99, 0.626, 0.808),
        SpotifyAudioFeaturesResponse("2NRANZE9UCmPAS5XVbXL40", 1,1,187800.0,96.083, 0.815, 0.457),
        SpotifyAudioFeaturesResponse("24JygzOLM0EmRQeGtFcIcG", 4,1,497493.0,115.7, 0.402, 0.281)
      ))))
    }

    "return playlist response when success" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "playlists", "playlist-1")) =>
            Response.ok(json("spotify/api/playlist-response.json"))
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.getPlaylist[IO]("token", "playlist-1")

      response.asserting(_ must be (SpotifyPlaylistResponse(
        "59ZbFPES4DQwEjBpWHzrtC",
        "Dinner with Friends",
        Some("Having friends over for dinner? Here´s the perfect playlist."),
        PlaylistTracks(Vector(PlaylistItem(PlaylistTrack(
          "4i9sYtSIlR80bxje5B3rUb",
          "I'm Not The Only One - Radio Edit",
          PlaylistTrackAlbum("5GWoXPsTQylMuaZ84PC563", "single", "I'm Not The Only One", Some("2012-10-10"), Some("day"), List(PlaylistTrackAlbumImage("https://i.scdn.co/image/47421900e7534789603de84c03a40a826c058e45",640,640), PlaylistTrackAlbumImage("https://i.scdn.co/image/0d447b6faae870f890dc5780cc58d9afdbc36a1d",300,300), PlaylistTrackAlbumImage("https://i.scdn.co/image/d926b3e5f435ef3ac0874b1ff1571cf675b3ef3b",64,64))),
          List(PlaylistTrackArtist("2wY79sveU1sp5g7SokKOiI", "Sam Smith")),
          45.0,
          "spotify:track:4i9sYtSIlR80bxje5B3rUb",
          PlaylistTrackUrls("https://open.spotify.com/track/4i9sYtSIlR80bxje5B3rUb")
        ))),105)
      )))
    }

    "return current user playlists response when success" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "me", "playlists")) =>
            Response.ok(json("spotify/api/playlists-response.json"))
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.getUserPlaylists[IO]("token")

      response.asserting(_ must be (SpotifyPlaylistsResponse(List(
        PlaylistsItem("53Y8wT46QIMz5H4WQ8O22c", "Wizzlers Big Playlist"),
        PlaylistsItem("1AVZz0mBuGbCEoNRQdYQju", "Another Playlist")),9)))
    }

    "return error when corrupted json" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "me", "playlists"))  => Response.ok("""{"foo"}""")
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = SpotifyRestApi.getUserPlaylists[IO]("token")

      response.attempt.asserting { error =>
        error mustBe Left(SpotifyApiError("error deserializing spotify response: expected : got '}' (line 1, column 7)"))
      }
    }

    "create playlist for a user when success" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "users", "user-1", "playlists")) && hasBody(r, """{"name":"my-playlist","description":"new-playlist-to-be-created","public":true,"collaborative":false}""") =>
            Response.ok(json("spotify/api/playlist-response.json"))
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.createPlaylist[IO]("token", "user-1", "my-playlist", Some("new-playlist-to-be-created"))

      response.asserting(_ must be (SpotifyPlaylistResponse(
        "59ZbFPES4DQwEjBpWHzrtC",
        "Dinner with Friends",
        Some("Having friends over for dinner? Here´s the perfect playlist."),
        PlaylistTracks(Vector(PlaylistItem(PlaylistTrack(
          "4i9sYtSIlR80bxje5B3rUb",
          "I'm Not The Only One - Radio Edit",
          PlaylistTrackAlbum("5GWoXPsTQylMuaZ84PC563", "single", "I'm Not The Only One", Some("2012-10-10"), Some("day"), List(PlaylistTrackAlbumImage("https://i.scdn.co/image/47421900e7534789603de84c03a40a826c058e45",640,640), PlaylistTrackAlbumImage("https://i.scdn.co/image/0d447b6faae870f890dc5780cc58d9afdbc36a1d",300,300), PlaylistTrackAlbumImage("https://i.scdn.co/image/d926b3e5f435ef3ac0874b1ff1571cf675b3ef3b",64,64))),
          List(PlaylistTrackArtist("2wY79sveU1sp5g7SokKOiI", "Sam Smith")),
          45.0,
          "spotify:track:4i9sYtSIlR80bxje5B3rUb",
          PlaylistTrackUrls("https://open.spotify.com/track/4i9sYtSIlR80bxje5B3rUb")
        ))),105)
      )))
    }

    "add tracks to a playlist" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "playlists", "playlist-1", "tracks")) && hasBody(r, """{"uris":["uri-1","uri-2","uri-3"],"position":null}""") =>
            Response(json("spotify/api/operation-success-response.json"), StatusCode.Created)
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.addTracksToPlaylist[IO]("token", "playlist-1", List("uri-1", "uri-2", "uri-3"))

      response.asserting(_ must be (SpotifyOperationSuccessResponse("JbtmHBDBAYu3/bt8BOXKjzKx3i0b6LCa/wVjyl6qQ2Yf6nFXkbmzuEa+ZI/U1yF+")))
    }

    "replace tracks in a playlist" in {
      implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "playlists", "playlist-1", "tracks")) && hasBody(r, """{"uris":["uri-1","uri-2","uri-3"]}""", Method.PUT) =>
            Response(json("spotify/api/operation-success-response.json"), StatusCode.Created)
          case _ => throw new RuntimeException()
        }

      val response = SpotifyRestApi.replaceTracksInPlaylist[IO]("token", "playlist-1", List("uri-1", "uri-2", "uri-3"))

      response.asserting(_ must be (()))
    }
  }

  def isAuthorized(req: client3.Request[_, _], host: String, paths: Seq[String] = Nil, token: String = "token"): Boolean =
    req.uri.host.contains(host) && (paths.isEmpty || req.uri.path == paths) &&
      req.headers.contains(new Header("Authorization", s"Bearer $token"))

  def hasBody(req: client3.Request[_, _], jsonBody: String, method: Method = Method.POST): Boolean =
    req.method == method && req.body.toString.contains(jsonBody)
}

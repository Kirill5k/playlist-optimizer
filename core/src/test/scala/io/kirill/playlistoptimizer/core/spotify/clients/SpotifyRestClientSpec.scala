package io.kirill.playlistoptimizer.core.spotify.clients

import cats.effect.IO
import io.kirill.playlistoptimizer.core.ApiClientSpec
import io.kirill.playlistoptimizer.core.RequestOps.*
import io.kirill.playlistoptimizer.core.common.errors.SpotifyTrackNotFound
import io.kirill.playlistoptimizer.core.playlist.Key.*
import io.kirill.playlistoptimizer.core.playlist.*
import sttp.client3.Response
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.testing.SttpBackendStub

import java.time.LocalDate
import scala.concurrent.duration.*

class SpotifyRestClientSpec extends ApiClientSpec {

  val token = "token-5lcpIsBqfb0Slx9fzZuCu_rM3aBDg"

  "A SpotifyRestClient" - {

    "create new playlist" in {
      implicit val testingBackend: SttpBackendStub[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.hasBearerToken(token) && r.isGoingTo("api.spotify.com/v1/users/user-1/playlists") && r.isPost && r.hasBody("""{"name":"Mel","description":"Melodic deep house and techno songs","public":true,"collaborative":false}""") =>
            Response.ok(json("spotify/flow/create/1-new-playlist.json"))
          case r if r.hasBearerToken(token) && r.isGoingTo("api.spotify.com/v1/playlists/7d2D2S200NyUE5KYs80PwO/tracks") && r.isPost =>
            Response.ok(json("spotify/flow/create/2-add-tracks.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = new LiveSpotifyRestClient[IO]().createPlaylist(token, "user-1", PlaylistBuilder.playlist)

      response.asserting(_ must be (()))
    }

    "find playlist by name" in {
      implicit val testingBackend: SttpBackendStub[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.hasBearerToken(token) && r.isGoingTo("api.spotify.com/v1/me/playlists") && r.isGet =>
            Response.ok(json("spotify/flow/find/2-users-playlists.json"))
          case r if r.hasBearerToken(token) && r.isGoingTo("api.spotify.com/v1/playlists/7npAZEYwEwV2JV7XX2n3wq") && r.isGet =>
            Response.ok(json("spotify/flow/find/3-playlist.json"))
          case r if r.hasBearerToken(token) && r.isGoingTo("api.spotify.com/v1/audio-features") =>
            Response.ok(json(s"spotify/flow/find/4-audio-features.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = new LiveSpotifyRestClient[IO]().findPlaylistByName(token, "mel")

      response.asserting { pl =>
        pl.name must be("Mel")
        pl.description must be(Some("Melodic deep house and techno songs"))
        pl.tracks must have size 46
        pl.tracks.head must be(Track(SongDetails("Glue", List("Bicep"), Release("Bicep", "album", Some(LocalDate.of(2017, 9, 1)), Some("GBCFB1700229")), Some("https://i.scdn.co/image/ab67616d0000b273d4322a9004288009f6da2975")), AudioDetails(129.983, 269150.milliseconds, CMinor, 0.853,0.798),SourceDetails("spotify:track:2aJDlirz6v2a4HREki98cP", Some("https://open.spotify.com/track/2aJDlirz6v2a4HREki98cP"))))
        pl.source must be(PlaylistSource.Spotify)
      }
    }

    "return all playlists that belong to a user" in {
      implicit val testingBackend: SttpBackendStub[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.hasBearerToken(token) && r.isGoingTo("api.spotify.com/v1/me/playlists") && r.isGet =>
            Response.ok(json("spotify/flow/get/2-users-playlists.json"))
          case r if r.hasBearerToken(token) && r.isGoingTo("api.spotify.com/v1/playlists") && r.isGet =>
            Response.ok(json(s"spotify/flow/get/3-playlist-${r.uri.path.last}.json"))
          case r if r.hasBearerToken(token) && r.isGoingTo("api.spotify.com/v1/audio-features") && r.isGet =>
            Response.ok(json(s"spotify/flow/find/4-audio-features.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = new LiveSpotifyRestClient[IO]().getAllPlaylists(token)

      response.asserting(_.map(_.name) must be (List("Mel 1", "Mel 2")))
    }

    "find track by name" in {
      implicit val testingBackend: SttpBackendStub[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.hasBearerToken(token) && r.isGoingTo("api.spotify.com/v1/search") =>
            Response.ok(json("spotify/flow/search/1-search-track.json"))
          case r if r.hasBearerToken(token) && r.isGoingTo("api.spotify.com/v1/audio-features") =>
            Response.ok(json("spotify/flow/search/2-audio-features.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = new LiveSpotifyRestClient[IO]().findTrackByName(token, "bicep glue")

      response.asserting { t =>
        t.song.name mustBe "Glue"
      }
    }

    "return track not found when search result empty" in {
      implicit val testingBackend: SttpBackendStub[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.hasBearerToken(token) && r.isGoingTo("api.spotify.com/v1/search") =>
            Response.ok(json("spotify/flow/search/3-search-empty.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = new LiveSpotifyRestClient[IO]().findTrackByName(token, "bicep glue")

      response.assertThrows[SpotifyTrackNotFound]
    }
  }
}

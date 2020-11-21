package io.kirill.playlistoptimizer.core.spotify.clients

import java.time.LocalDate

import cats.effect.IO
import io.kirill.playlistoptimizer.core.ApiClientSpec
import io.kirill.playlistoptimizer.core.common.errors.SpotifyTrackNotFound
import io.kirill.playlistoptimizer.core.playlist.Key._
import io.kirill.playlistoptimizer.core.playlist._
import sttp.client
import sttp.client.Response
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.{Header, Method}

import scala.concurrent.duration._

class SpotifyApiClientSpec extends ApiClientSpec {

  val token = "token-5lcpIsBqfb0Slx9fzZuCu_rM3aBDg"

  "A SpotifyClient" - {

    "create new playlist" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "users", "user-1", "playlists")) && r.method == Method.POST && r.body.toString.contains("""{"name":"Mel","description":"Melodic deep house and techno songs","public":true,"collaborative":false}""") =>
            Response.ok(json("spotify/flow/create/1-new-playlist.json"))
          case r if isAuthorized(r, "api.spotify.com", List("v1", "playlists", "7d2D2S200NyUE5KYs80PwO", "tracks")) && r.method == Method.POST =>
            Response.ok(json("spotify/flow/create/2-add-tracks.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = new SpotifyApiClient[IO]().createPlaylist(token, "user-1", PlaylistBuilder.playlist)

      response.asserting(_ must be (()))
    }

    "find playlist by name" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "me", "playlists")) => Response.ok(json("spotify/flow/find/2-users-playlists.json"))
          case r if isAuthorized(r, "api.spotify.com", List("v1", "playlists", "7npAZEYwEwV2JV7XX2n3wq")) => Response.ok(json("spotify/flow/find/3-playlist.json"))
          case r if isAuthorized(r, "api.spotify.com", List("v1", "audio-features")) => Response.ok(json(s"spotify/flow/find/4-audio-features.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = new SpotifyApiClient[IO]().findPlaylistByName(token, "mel")

      response.asserting { pl =>
        pl.name must be("Mel")
        pl.description must be(Some("Melodic deep house and techno songs"))
        pl.tracks must have size 46
        pl.tracks.head must be(Track(SongDetails("Glue", List("Bicep"), Some("Bicep"), Some(LocalDate.of(2017, 9, 1)), Some("album"), Some("https://i.scdn.co/image/ab67616d0000b273d4322a9004288009f6da2975")), AudioDetails(129.983, 269150.milliseconds, CMinor, 0.853,0.798),SourceDetails("spotify:track:2aJDlirz6v2a4HREki98cP", Some("https://open.spotify.com/track/2aJDlirz6v2a4HREki98cP"))))
        pl.source must be(PlaylistSource.Spotify)
      }
    }

    "return all playlists that belong to a user" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "me", "playlists")) => Response.ok(json("spotify/flow/get/2-users-playlists.json"))
          case r if isAuthorized(r, "api.spotify.com", List("v1", "playlists")) => Response.ok(json(s"spotify/flow/get/3-playlist-${r.uri.path.last}.json"))
          case r if isAuthorized(r, "api.spotify.com", List("v1", "audio-features")) => Response.ok(json(s"spotify/flow/find/4-audio-features.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = new SpotifyApiClient[IO]().getAllPlaylists(token)

      response.asserting(_.map(_.name) must be (List("Mel 1", "Mel 2")))
    }

    "find track by name" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "search")) => Response.ok(json("spotify/flow/search/1-search-track.json"))
          case r if isAuthorized(r, "api.spotify.com", List("v1", "audio-features")) => Response.ok(json("spotify/flow/search/2-audio-features.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = new SpotifyApiClient[IO]().findTrackByName(token, "bicep glue")

      response.asserting { t =>
        t.song.name must be ("Glue")
      }
    }

    "return track not found when search result empty" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if isAuthorized(r, "api.spotify.com", List("v1", "search")) => Response.ok(json("spotify/flow/search/3-search-empty.json"))
          case r => throw new RuntimeException(s"no mocks for ${r.uri.host}/${r.uri.path.mkString("/")}")
        }

      val response = new SpotifyApiClient[IO]().findTrackByName(token, "bicep glue")

      response.assertThrows[SpotifyTrackNotFound]
    }
  }

  def isAuthorized(req: client.Request[_, _], host: String, paths: Seq[String] = Nil): Boolean =
    req.uri.host == host && (paths.isEmpty || req.uri.path.startsWith(paths)) &&
      req.headers.contains(new Header("Authorization", "Bearer token-5lcpIsBqfb0Slx9fzZuCu_rM3aBDg"))

}

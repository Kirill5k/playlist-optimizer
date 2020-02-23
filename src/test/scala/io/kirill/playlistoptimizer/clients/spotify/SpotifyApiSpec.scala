package io.kirill.playlistoptimizer.clients.spotify

import java.io.File

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import cats.implicits._
import io.kirill.playlistoptimizer.clients.spotify.SpotifyError._
import io.kirill.playlistoptimizer.clients.spotify.SpotifyResponse._
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client.Response
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.{Header, Method, StatusCode}

import scala.concurrent.ExecutionContext
import scala.io.Source

class SpotifyApiSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  implicit val spotifyConfig = SpotifyConfig("http://spotify.com", "/auth", "/playlists", "/audio-analysis", "client-id", "client-secret")

  val authSuccessResponseJson = Source.fromResource("spotify/auth-response.json").getLines.toList.mkString
  val audioAnalysisResponseJson = Source.fromResource("spotify/audio-analysis-response.json").getLines.toList.mkString
  val playlistResponseJson = Source.fromResource("spotify/playlist-response.json").getLines.toList.mkString
  val authErrorResponseJson = Source.fromResource("spotify/auth-error.json").getLines.toList.mkString

  "A SpotifyApi" - {
    "return auth response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "spotify.com/auth" && r.method == Method.POST => Response.ok(authSuccessResponseJson)
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyApi.authenticate[IO]

      authResponse.asserting(_ must be (SpotifyAuthResponse("BQC3wD_w-ODtKQsbz7woOZPvffQX5iX7rychivVGQxO3qzgejLCgXwAE5acsqk8LQcih2qpDkaCjrJRRhuY", "Bearer", 3600, "")))
    }

    "return auth error when failure" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "spotify.com/auth" && r.method == Method.POST => Response(authErrorResponseJson, StatusCode.InternalServerError)
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyApi.authenticate[IO]

      authResponse.assertThrows[SpotifyAuthError]
    }

    "return audio analysis response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "spotify.com/audio-analysis" && r.uri.path == List("track-1") && r.method == Method.GET && r.headers.contains(new Header("Authorization", "Bearer token")) =>
            Response.ok(audioAnalysisResponseJson)
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyApi.getAudioAnalysis[IO]("token", "track-1")

      authResponse.asserting(_ must be (SpotifyAudioAnalysisResponse(AudioAnalysisTrack(255.34898, 98.002, 5, 0))))
    }

    "return playlist response when success" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatchesPartial {
          case r if r.uri.host == "spotify.com/playlists" && r.uri.path == List("playlist-1") && r.method == Method.GET && r.headers.contains(new Header("Authorization", "Bearer token")) =>
            Response.ok(playlistResponseJson)
          case _ => throw new RuntimeException()
        }

      val authResponse = SpotifyApi.getPlaylist[IO]("token", "playlist-1")

      authResponse.asserting(_ must be (SpotifyPlaylistResponse(
        "59ZbFPES4DQwEjBpWHzrtC",
        "Dinner with Friends",
        "Having friends over for dinner? Here´s the perfect playlist.",
        PlaylistTracks(List(PlaylistItem(PlaylistTrack("4i9sYtSIlR80bxje5B3rUb", "I'm Not The Only One - Radio Edit", PlaylistTrackAlbum("5GWoXPsTQylMuaZ84PC563", "single", "I'm Not The Only One"), List(PlaylistTrackArtist("2wY79sveU1sp5g7SokKOiI", "Sam Smith")),45.0))),105)
      )))
    }
  }
}

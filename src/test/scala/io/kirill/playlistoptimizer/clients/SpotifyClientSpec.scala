package io.kirill.playlistoptimizer.clients

import io.kirill.playlistoptimizer.configs.{SpotifyApiConfig, SpotifyAuthConfig, SpotifyConfig}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.io.Source

class SpotifyClientSpec extends AnyWordSpec with Matchers {

  val authConfig = SpotifyAuthConfig("http://account.spotify.com", "/auth", "client-id", "client-secret", "user-id")
  val apiConfig = SpotifyApiConfig("http://api.spotify.com", "/users", "/playlists", "/audio-analysis")
  implicit val spotifyConfig = SpotifyConfig(authConfig, apiConfig)

  val authResponseJson = Source.fromResource("spotify/flow/1-auth.json").getLines.toList.mkString
  val usersPlaylistResponseJson = Source.fromResource("spotify/flow/1-users-playlists.json").getLines.toList.mkString
  val playlistResponseJson = Source.fromResource("spotify/flow/3-playlist.json").getLines.toList.mkString
  val audioAnalysis1ResponseJson = Source.fromResource("spotify/flow/4-audio-analysis-1.json").getLines.toList.mkString
  val audioAnalysis2ResponseJson = Source.fromResource("spotify/flow/5-audio-analysis-2.json").getLines.toList.mkString

  "A SpotifyClient" should {

  }
}

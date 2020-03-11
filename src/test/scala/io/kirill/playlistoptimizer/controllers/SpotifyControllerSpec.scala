package io.kirill.playlistoptimizer.controllers

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ContextShift, IO}
import io.kirill.playlistoptimizer.configs.{SpotifyApiConfig, SpotifyAuthConfig, SpotifyConfig}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.ExecutionContext

class SpotifyControllerSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  val authConfig = SpotifyAuthConfig("http://account.spotify.com", "/authorize", "/token", "client-id", "client-secret", "/redirect")
  val apiConfig = SpotifyApiConfig("http://api.spotify.com", "/me", "/users", "/playlists", "/audio-analysis", "/audio-features")
  implicit val spotifyConfig = SpotifyConfig(authConfig, apiConfig)

}

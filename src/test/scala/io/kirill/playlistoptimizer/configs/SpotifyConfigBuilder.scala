package io.kirill.playlistoptimizer.configs

object SpotifyConfigBuilder {

  def testConfig: SpotifyConfig = {
    val authConfig = SpotifyAuthConfig("http://account.spotify.com", "/authorize", "/token", "client-id", "client-secret", "/redirect")
    val apiConfig = SpotifyApiConfig("http://api.spotify.com", "/me", "/users", "/playlists", "/audio-analysis", "/audio-features")
    SpotifyConfig(authConfig, apiConfig)
  }
}

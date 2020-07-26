package io.kirill.playlistoptimizer.common.configs

object SpotifyConfigBuilder {

  def testConfig: SpotifyConfig = {
    SpotifyConfig("http://account.spotify.com", "http://api.spotify.com", "/authorize", "client-id", "client-secret", "/redirect")
  }
}

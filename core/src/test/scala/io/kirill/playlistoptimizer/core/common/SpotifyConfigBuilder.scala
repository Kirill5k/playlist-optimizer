package io.kirill.playlistoptimizer.core.common

import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig

object SpotifyConfigBuilder {

  def testConfig: SpotifyConfig = {
    SpotifyConfig("http://account.spotify.com", "http://api.spotify.com", "/authorize", "client-id", "client-secret", "/redirect")
  }
}

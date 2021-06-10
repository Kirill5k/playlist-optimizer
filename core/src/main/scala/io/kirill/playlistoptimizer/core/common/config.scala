package io.kirill.playlistoptimizer.core.common

import pureconfig.*
import pureconfig.generic.derivation.default.*

object config {

  final case class ServerConfig(
      host: String,
      port: Int
  ) derives ConfigReader

  final case class SpotifyConfig(
      authUrl: String,
      restUrl: String,
      clientId: String,
      clientSecret: String,
      redirectUrl: String,
      homepageUrl: String
  ) derives ConfigReader
  
  final case class JwtConfig(
      alg: String,
      secret: String
  ) derives ConfigReader

  final case class AppConfig(
      server: ServerConfig,
      spotify: SpotifyConfig,
      jwt: JwtConfig
  ) derives ConfigReader

  object AppConfig {
    def load(): AppConfig = ConfigSource.default.loadOrThrow[AppConfig]

  }

}

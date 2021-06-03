package io.kirill.playlistoptimizer.core.common

import pureconfig._
import pureconfig.generic.auto._

object config {

  final case class ServerConfig(
      host: String,
      port: Int
  )

  final case class SpotifyConfig(
      authUrl: String,
      restUrl: String,
      clientId: String,
      clientSecret: String,
      redirectUrl: String,
      homepageUrl: String
  )

  final case class JwtConfig(
      alg: String,
      secret: String
  )

  final case class AppConfig(
      server: ServerConfig,
      spotify: SpotifyConfig,
      jwt: JwtConfig
  )

  object AppConfig {
    def load(): AppConfig =
      ConfigSource.default.loadOrThrow[AppConfig]
  }

}

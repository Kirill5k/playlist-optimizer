package io.kirill.playlistoptimizer.core.common

import cats.effect.{Blocker, ContextShift, IO, Sync}
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

object config {

  final case class GeneticAlgorithmConfig(
      populationSize: Int,
      iterations: Int,
      mutationFactor: Double
  )

  final case class AlgorithmsConfig(
      ga: GeneticAlgorithmConfig
  )

  final case class ServerConfig(
      host: String,
      port: Int
  )

  final case class SpotifyConfig(
      authUrl: String,
      restUrl: String,
      clientId: String,
      clientSecret: String,
      redirectUri: String
  )

  final case class AppConfig(
      server: ServerConfig,
      spotify: SpotifyConfig,
      algorithms: AlgorithmsConfig
  )

  object AppConfig {
    def load(): AppConfig =
      ConfigSource.default.loadOrThrow[AppConfig]

    def loadF[F[_]: Sync: ContextShift](blocker: Blocker): F[AppConfig] =
      ConfigSource.default.loadF[F, AppConfig](blocker)
  }

}

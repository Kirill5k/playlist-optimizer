package io.kirill.playlistoptimizer.core.common

import cats.effect.{Blocker, ContextShift, IO}
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
      hostname: String,
      port: Int
  )

  final case class SpotifyConfig(
      authUrl: String,
      restUrl: String,
      authorizationPath: String,
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
    def load(blocker: Blocker)(implicit cs: ContextShift[IO]): IO[AppConfig] =
      ConfigSource.default.loadF[IO, AppConfig](blocker)
  }

}

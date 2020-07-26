package io.kirill.playlistoptimizer.common.configs

import cats.effect.{Blocker, ContextShift, IO}
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

final case class AppConfig(server: ServerConfig, spotify: SpotifyConfig, algorithms: AlgorithmsConfig)

object AppConfig {
  def load(blocker: Blocker)(implicit cs: ContextShift[IO]): IO[AppConfig] = {
    ConfigSource.default.loadF[IO, AppConfig](blocker)
  }
}

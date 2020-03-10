package io.kirill.playlistoptimizer.configs

import cats.effect.{Blocker, ContextShift, IO}
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

final case class MainConfig(server: ServerConfig, spotify: SpotifyConfig)

object MainConfig {
  def load(blocker: Blocker)(implicit cs: ContextShift[IO]): IO[MainConfig] = {
    ConfigSource.default.loadF[IO, MainConfig](blocker)
  }
}

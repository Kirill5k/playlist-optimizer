package io.kirill.playlistoptimizer.core.common

import cats.effect.IO
import io.kirill.playlistoptimizer.core.common.config.AppConfig
import io.kirill.playlistoptimizer.domain.CatsIOSpec

class AppConfigSpec extends CatsIOSpec {

  "A MainConfig" - {
    "should be parsed from application.conf" in {
      AppConfig.load[IO].asserting { config =>
        config.server.port mustBe 5000
        config.spotify.authUrl mustBe "https://accounts.spotify.com"
      }
    }
  }
}

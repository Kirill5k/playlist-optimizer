package io.kirill.playlistoptimizer.core.common

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.kirill.playlistoptimizer.core.common.config.AppConfig
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class AppConfigSpec extends AsyncWordSpec with Matchers {

  "A MainConfig" should {
    "be parsed from application.conf" in {
      AppConfig.load[IO].unsafeToFuture().map { config =>
        config.server.port mustBe 5000
        config.spotify.authUrl mustBe "https://accounts.spotify.com"
      }
    }
  }
}

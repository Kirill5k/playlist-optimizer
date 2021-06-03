package io.kirill.playlistoptimizer.core.common

import io.kirill.playlistoptimizer.core.common.config.AppConfig
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AppConfigSpec extends AnyWordSpec with Matchers {

  "A MainConfig" should {
    "be parsed from application.conf" in {
      val config = AppConfig.load()

      config.server.port mustBe 5000
      config.spotify.authUrl mustBe "https://accounts.spotify.com"
    }
  }
}

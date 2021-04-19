package io.kirill.playlistoptimizer.core.common

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import io.kirill.playlistoptimizer.core.common.config.AppConfig
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

class AppConfigSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "A MainConfig" - {
    "should be parsed from application.conf" in {
      val config = AppConfig.loadF[IO]

      config.asserting(_ mustBe a[AppConfig])
    }
  }
}

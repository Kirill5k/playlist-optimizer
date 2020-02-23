package io.kirill.playlistoptimizer.configs

import org.scalatest.matchers.must.Matchers
import pureconfig._
import pureconfig.generic.auto._
import org.scalatest.wordspec.AnyWordSpec

class MainConfigSpec extends AnyWordSpec with Matchers {

  "A MainConfig" should {
    "be parsed from application.conf" in {
      val config = ConfigSource.default.loadOrThrow[MainConfig]

      config mustBe a [MainConfig]
    }
  }
}

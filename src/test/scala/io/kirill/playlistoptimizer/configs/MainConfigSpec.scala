package io.kirill.playlistoptimizer.configs

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{Blocker, ContextShift, IO}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.ExecutionContext

class MainConfigSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  "A MainConfig" - {
    "be parsed from application.conf" in {
      val config = Blocker[IO].use(b => MainConfig.load(b))

      config.asserting(_ mustBe a [MainConfig])
    }
  }
}

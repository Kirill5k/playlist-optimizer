package io.kirill.playlistoptimizer.core

import cats.effect.{ContextShift, IO}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

trait ControllerSpec extends AnyWordSpec with MockitoSugar with ArgumentMatchersSugar with Matchers {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)
}

package io.kirill.playlistoptimizer.core

import cats.effect.{ContextShift, IO}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser._
import io.kirill.playlistoptimizer.core.common.json._
import org.http4s.{Response, Status}
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._

import scala.concurrent.ExecutionContext

trait ControllerSpec extends AnyWordSpec with MockitoSugar with ArgumentMatchersSugar with Matchers {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  def verifyResponse[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A] = None, cookies: Map[String, String] = Map())(implicit dec: EntityDecoder[IO, A]): Unit = {
    val actualResp = actual.unsafeRunSync

    actualResp.status must be (expectedStatus)
    actualResp.cookies.map(c => (c.name -> c.content)) must contain allElementsOf cookies
    expectedBody match {
      case Some(expected) => actualResp.as[A].unsafeRunSync must be (expected)
      case None => actualResp.body.compile.toVector.unsafeRunSync mustBe empty
    }
  }

  def verifyJsonResponse(actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[String] = None): Unit = {
    val actualResp = actual.unsafeRunSync

    actualResp.status must be (expectedStatus)
    expectedBody match {
      case Some(expected) => actualResp.asJson.unsafeRunSync() must be (parse(expected).getOrElse(throw new RuntimeException))
      case None => actualResp.body.compile.toVector.unsafeRunSync mustBe empty
    }
  }
}

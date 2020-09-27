package io.kirill.playlistoptimizer.core

import cats.effect.{ContextShift, IO}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser._
import io.circe.literal._
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

  implicit val logger: Logger[IO]   = Slf4jLogger.getLogger[IO]
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)

  val shortenedPlaylistJson =
    json"""
      {
          "name" : "Mel",
          "description" : "Melodic deep house and techno songs",
          "source" : "Spotify",
          "tracks" : [
            {
              "name" : "Glue",
              "artists" : [
                "Bicep"
              ],
              "releaseName" : "Bicep",
              "releaseDate" : "2017-09-01",
              "releaseType" : "album",
              "artwork": null,
              "tempo" : 129.983,
              "duration" : 269.15,
              "key" : 5,
              "mode" : 0,
              "danceability": 0.613,
              "energy": 0.807,
              "uri" : "spotify:track:2aJDlirz6v2a4HREki98cP",
              "url" : "https://open.spotify.com/track/2aJDlirz6v2a4HREki98cP"
            }
          ]
        }
      """

  def verifyResponse[A](
      actual: IO[Response[IO]],
      expectedStatus: Status,
      expectedBody: Option[A] = None,
      cookies: Map[String, String] = Map()
  )(implicit dec: EntityDecoder[IO, A]): Unit = {
    val actualResp = actual.unsafeRunSync

    actualResp.status must be(expectedStatus)
    actualResp.cookies.map(c => (c.name -> c.content)) must contain allElementsOf cookies
    expectedBody match {
      case Some(expected) => actualResp.as[A].unsafeRunSync must be(expected)
      case None           => actualResp.body.compile.toVector.unsafeRunSync mustBe empty
    }
  }

  def verifyJsonResponse(
      actual: IO[Response[IO]],
      expectedStatus: Status,
      expectedBody: Option[String] = None,
      cookies: Map[String, String] = Map()
  ): Unit = {
    val actualResp = actual.unsafeRunSync

    actualResp.status must be(expectedStatus)
    actualResp.cookies.map(c => (c.name -> c.content)) must contain allElementsOf cookies
    expectedBody match {
      case Some(expected) => actualResp.asJson.unsafeRunSync() must be(parse(expected).getOrElse(throw new RuntimeException))
      case None           => actualResp.body.compile.toVector.unsafeRunSync mustBe empty
    }
  }
}

package io.kirill.playlistoptimizer.core

import cats.effect.{ContextShift, IO}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import io.circe.literal._
import io.circe.parser._
import org.http4s.circe._
import org.http4s.{Response, Status, _}
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

trait ControllerSpec extends AnyWordSpec with MockitoSugar with ArgumentMatchersSugar with Matchers {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val logger: Logger[IO]   = Slf4jLogger.getLogger[IO]

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
              "release" : {
                "name": "Awesome mix",
                "date": "2020-03-17",
                "kind": "compilation",
                "uid": null
              },
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
  )(implicit dec: EntityDecoder[IO, A]): Assertion = {
    val actualResp = actual.unsafeRunSync()

    actualResp.status must be(expectedStatus)
    actualResp.cookies.map(c => (c.name -> c.content)) must contain allElementsOf cookies
    expectedBody match {
      case Some(expected) => actualResp.as[A].unsafeRunSync() must be(expected)
      case None           => actualResp.body.compile.toVector.unsafeRunSync() mustBe empty
    }
  }

  def verifyJsonResponse(
      actual: IO[Response[IO]],
      expectedStatus: Status,
      expectedBody: Option[String] = None,
      cookies: Map[String, String] = Map.empty[String, String]
  ): Assertion = {
    val actualResp = actual.unsafeRunSync()

    actualResp.status must be(expectedStatus)
    actualResp.cookies.map(c => c.name -> c.content) must contain allElementsOf cookies
    expectedBody match {
      case Some(expected) => actualResp.asJson.unsafeRunSync() must be(parse(expected).getOrElse(throw new RuntimeException))
      case None           => actualResp.body.compile.toVector.unsafeRunSync() mustBe empty
    }
  }
}

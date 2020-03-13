package io.kirill.playlistoptimizer.playlist

import java.time.LocalDate

import cats.effect._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

class PlaylistControllerSpec extends AnyWordSpec with MockitoSugar with ArgumentMatchersSugar with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)
  implicit val dec1: EntityDecoder[IO, PlaylistView] = jsonOf[IO, PlaylistView]
  implicit val dec2: EntityDecoder[IO, Seq[PlaylistView]] = jsonOf[IO, Seq[PlaylistView]]

  val playlist = PlaylistBuilder.playlist

  "A PlaylistController" should {
    val playlistServiceMock = mock[PlaylistService[IO]]
    val playlistController = new PlaylistController[IO] {
      override protected val playlistService: PlaylistService[IO] = playlistServiceMock
    }

    "get all playlists" in {
      when(playlistServiceMock.getAll).thenReturn(IO.pure(List(playlist.copy(tracks = List(playlist.tracks.head)))))

      val request = Request[IO](uri = uri"/playlists")
      val response: IO[Response[IO]] = playlistController.routes.orNotFound.run(request)

      verify[Seq[PlaylistView]](response, Status.Ok, Some(List(PlaylistView("Mel", Some("Melodic deep house and techno songs"), "Spotify", List(
        TrackView("Glue", List("Bicep"), Some("Bicep"), Some(LocalDate.of(2017, 9, 1)), Some("album"), 129.983, 269.15, 5, 0, "spotify:track:2aJDlirz6v2a4HREki98cP", Some("https://open.spotify.com/track/2aJDlirz6v2a4HREki98cP"))
      )))))
    }
  }

  def verify[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A] = None)(implicit ev: EntityDecoder[IO, A]): Unit = {
    val actualResp = actual.unsafeRunSync

    actualResp.status must be (expectedStatus)
    expectedBody match {
      case Some(expected) => actualResp.as[A].unsafeRunSync must be (expected)
      case None => actualResp.body.compile.toVector.unsafeRunSync mustBe empty
    }
  }
}

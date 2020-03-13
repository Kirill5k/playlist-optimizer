package io.kirill.playlistoptimizer.playlist

import java.time.LocalDate

import cats.effect._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.mockito.{ArgumentCaptor, ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

class PlaylistControllerSpec extends AnyWordSpec with MockitoSugar with ArgumentMatchersSugar with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)
  implicit val dec1: EntityDecoder[IO, PlaylistView] = jsonOf[IO, PlaylistView]
  implicit val dec2: EntityDecoder[IO, Seq[PlaylistView]] = jsonOf[IO, Seq[PlaylistView]]

  val playlist = PlaylistBuilder.playlist
  val shortenedPlaylist = playlist.copy(tracks = List(playlist.tracks.head))

  val shortenedPlaylistJson =
    """
      |{
      |    "name" : "Mel",
      |    "description" : "Melodic deep house and techno songs",
      |    "source" : "Spotify",
      |    "tracks" : [
      |      {
      |        "name" : "Glue",
      |        "artists" : [
      |          "Bicep"
      |        ],
      |        "releaseName" : "Bicep",
      |        "releaseDate" : "2017-09-01",
      |        "releaseType" : "album",
      |        "tempo" : 129.983,
      |        "duration" : 269.15,
      |        "key" : 5,
      |        "mode" : 0,
      |        "uri" : "spotify:track:2aJDlirz6v2a4HREki98cP",
      |        "url" : "https://open.spotify.com/track/2aJDlirz6v2a4HREki98cP"
      |      }
      |    ]
      |  }
      |""".stripMargin

  "A PlaylistController" should {
    val playlistServiceMock = mock[PlaylistService[IO]]
    val playlistController = new PlaylistController[IO] {
      override protected val playlistService: PlaylistService[IO] = playlistServiceMock
    }

    "get all playlists" in {
      when(playlistServiceMock.getAll).thenReturn(IO.pure(List(shortenedPlaylist)))

      val request = Request[IO](uri = uri"/playlists")
      val response: IO[Response[IO]] = playlistController.routes.orNotFound.run(request)

      verifyResponse[Seq[PlaylistView]](response, Status.Ok, Some(List(PlaylistView("Mel", Some("Melodic deep house and techno songs"), "Spotify", List(
        TrackView("Glue", List("Bicep"), Some("Bicep"), Some(LocalDate.of(2017, 9, 1)), Some("album"), 129.983, 269.15, 5, 0, "spotify:track:2aJDlirz6v2a4HREki98cP", Some("https://open.spotify.com/track/2aJDlirz6v2a4HREki98cP"))
      )))))
      verify(playlistServiceMock).getAll
    }

    "create new playlist" in {
      val playlistCaptor: ArgumentCaptor[Playlist] = ArgumentCaptor.forClass(classOf[Playlist])
      when(playlistServiceMock.save(playlistCaptor.capture())).thenReturn(IO.pure(()))

      val request = Request[IO](uri = uri"/playlists", method = Method.POST).withEntity(shortenedPlaylistJson)
      val response: IO[Response[IO]] = playlistController.routes.orNotFound.run(request)

      verifyResponse[PlaylistView](response, Status.Created)
      playlistCaptor.getValue must be (shortenedPlaylist)
    }

    "optimize playlist" in {
      val playlistCaptor: ArgumentCaptor[Playlist] = ArgumentCaptor.forClass(classOf[Playlist])
      when(playlistServiceMock.optimize(playlistCaptor.capture())(any)).thenReturn(IO.pure(shortenedPlaylist.copy(name = s"Mel Optimized")))

      val request = Request[IO](uri = uri"/playlists/optimize", method = Method.POST).withEntity(shortenedPlaylistJson)
      val response: IO[Response[IO]] = playlistController.routes.orNotFound.run(request)

      verifyResponse[PlaylistView](response, Status.Ok, Some(PlaylistView("Mel Optimized", Some("Melodic deep house and techno songs"), "Spotify", List(
        TrackView("Glue", List("Bicep"), Some("Bicep"), Some(LocalDate.of(2017, 9, 1)), Some("album"), 129.983, 269.15, 5, 0, "spotify:track:2aJDlirz6v2a4HREki98cP", Some("https://open.spotify.com/track/2aJDlirz6v2a4HREki98cP"))
      ))))
      playlistCaptor.getValue must be (shortenedPlaylist)
    }
  }

  def verifyResponse[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A] = None)(implicit ev: EntityDecoder[IO, A]): Unit = {
    val actualResp = actual.unsafeRunSync

    actualResp.status must be (expectedStatus)
    expectedBody match {
      case Some(expected) => actualResp.as[A].unsafeRunSync must be (expected)
      case None => actualResp.body.compile.toVector.unsafeRunSync mustBe empty
    }
  }
}

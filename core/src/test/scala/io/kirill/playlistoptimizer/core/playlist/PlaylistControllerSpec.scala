package io.kirill.playlistoptimizer.core.playlist

import java.time.{Instant, LocalDate}
import java.util.UUID

import cats.effect._
import io.circe._
import io.circe.generic.auto._
import io.circe.literal._
import io.circe.parser._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.ControllerSpec
import io.kirill.playlistoptimizer.core.common.json._
import io.kirill.playlistoptimizer.core.common.controllers.AppController
import io.kirill.playlistoptimizer.core.optimizer.PlaylistOptimizer.{Optimization, OptimizationId}
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.mockito.ArgumentCaptor


class PlaylistControllerSpec extends ControllerSpec {
  import PlaylistController._
  import AppController._

  val playlist = PlaylistBuilder.playlist
  val shortenedPlaylist = playlist.copy(tracks = List(playlist.tracks.head))

  val optimizationId = OptimizationId(UUID.fromString("607995e0-8e3a-11ea-bc55-0242ac130003"))
  val optimization = Optimization(optimizationId, "in progress", shortenedPlaylist, Instant.parse("2020-01-01T00:00:00Z"))

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
              "tempo" : 129.983,
              "duration" : 269.15,
              "key" : 5,
              "mode" : 0,
              "uri" : "spotify:track:2aJDlirz6v2a4HREki98cP",
              "url" : "https://open.spotify.com/track/2aJDlirz6v2a4HREki98cP"
            }
          ]
        }
      """

  "A PlaylistController" should {
    val playlistServiceMock = mock[PlaylistService[IO]]
    val playlistController = new PlaylistController[IO] {
      override protected val playlistService: PlaylistService[IO] = playlistServiceMock
    }

    "get all playlists" in {
      when(playlistServiceMock.getAll).thenReturn(IO.pure(List(shortenedPlaylist)))

      val request = Request[IO](uri = uri"/playlists")
      val response: IO[Response[IO]] = playlistController.routes.orNotFound.run(request)

      val expected = List(PlaylistView(
        "Mel",
        Some("Melodic deep house and techno songs"),
        List(TrackView("Glue", List("Bicep"), Some("Bicep"), Some(LocalDate.of(2017, 9, 1)), Some("album"), 129.983, 269.15, 5, 0, "spotify:track:2aJDlirz6v2a4HREki98cP", Some("https://open.spotify.com/track/2aJDlirz6v2a4HREki98cP"))),
        "Spotify"
      ))

      verifyResponse[Seq[PlaylistView]](response, Status.Ok, Some(expected))
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

    "return internal server error if uncategorized error" in {
      when(playlistServiceMock.getAll).thenReturn(IO.raiseError(new NullPointerException("error-message")))

      val request = Request[IO](uri = uri"/playlists")
      val response: IO[Response[IO]] = playlistController.routes.orNotFound.run(request)

      verifyResponse[ErrorResponse](response, Status.InternalServerError, Some(ErrorResponse("error-message")))
    }
  }
}

package io.kirill.playlistoptimizer.core.optimizer

import java.time.Instant
import java.util.UUID

import cats.effect._
import io.circe.generic.auto._
import io.circe.literal._
import io.kirill.playlistoptimizer.core.ControllerSpec
import io.kirill.playlistoptimizer.core.common.controllers.AppController
import io.kirill.playlistoptimizer.core.common.errors.OptimizationNotFound
import io.kirill.playlistoptimizer.core.common.json._
import io.kirill.playlistoptimizer.core.playlist.{Playlist, PlaylistBuilder}
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.mockito.ArgumentCaptor


class OptimizationControllerSpec extends ControllerSpec {
  import AppController._

  val playlist = PlaylistBuilder.playlist
  val shortenedPlaylist = playlist.copy(tracks = Vector(playlist.tracks.head))

  val optimizationId = OptimizationId(UUID.fromString("607995e0-8e3a-11ea-bc55-0242ac130003"))
  val optimizationParameters = OptimizationParameters(100, 0.2, 1000, true)
  val optimization = Optimization(optimizationId, "in progress", optimizationParameters, shortenedPlaylist, Instant.parse("2020-01-01T00:00:00Z"))

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
    val playlistOptimizerMock = mock[PlaylistOptimizer[IO]]
    val playlistController = new OptimizationController[IO](playlistOptimizerMock)

    "initiate optimization of a playlist" in {
      val playlistCaptor: ArgumentCaptor[Playlist] = ArgumentCaptor.forClass(classOf[Playlist])
      val parametersCaptor: ArgumentCaptor[OptimizationParameters] = ArgumentCaptor.forClass(classOf[OptimizationParameters])
      when(playlistOptimizerMock.optimize(playlistCaptor.capture(), parametersCaptor.capture())).thenReturn(IO.pure(optimizationId))

      val requestBody =
        json"""
              {
                "playlist": $shortenedPlaylistJson,
                "optimizationParameters": {
                  "populationSize": 100,
                  "iterations": 1000,
                  "mutationFactor": 0.2,
                  "shuffle": true
                }
              }
              """

      val request = Request[IO](uri = uri"/playlist-optimizations", method = Method.POST).withEntity(requestBody)
      val response: IO[Response[IO]] = playlistController.routes.orNotFound.run(request)

      val expected =
        s"""
          |{"id": "${optimizationId.value}"}
          |""".stripMargin

      verifyJsonResponse(response, Status.Created, Some(expected))
      playlistCaptor.getValue must be (shortenedPlaylist)
      parametersCaptor.getValue must be (optimizationParameters)
    }

    "get playlist optimization by id" in {
      when(playlistOptimizerMock.get(optimizationId)).thenReturn(IO.pure(optimization))

      val request = Request[IO](uri = uri"/playlist-optimizations/607995e0-8e3a-11ea-bc55-0242ac130003", method = Method.GET)
      val response: IO[Response[IO]] = playlistController.routes.orNotFound.run(request)

      val expected =
        s"""
           |{
           |"id": "607995e0-8e3a-11ea-bc55-0242ac130003",
           |"status": "in progress",
           |"parameters": { "populationSize": 100, "iterations": 1000, "mutationFactor": 0.2, "shuffle": true},
           |"dateInitiated": "2020-01-01T00:00:00Z",
           |"original": {
           |    "name" : "Mel",
           |    "description" : "Melodic deep house and techno songs",
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
           |    ],
           |    "source" : "Spotify"
           |  },
           |"durationMs": null,
           |"result": null,
           |"score": null
           |}
           |""".stripMargin

      verifyJsonResponse(response, Status.Ok, Some(expected))
    }

    "return all optimizations" in {
      when(playlistOptimizerMock.getAll()).thenReturn(IO.pure(List(optimization)))

      val request = Request[IO](uri = uri"/playlist-optimizations", method = Method.GET)
      val response: IO[Response[IO]] = playlistController.routes.orNotFound.run(request)

      val expected =
        s"""
           |[{
           |"id": "607995e0-8e3a-11ea-bc55-0242ac130003",
           |"status": "in progress",
           |"parameters": { "populationSize": 100, "iterations": 1000, "mutationFactor": 0.2, "shuffle": true},
           |"dateInitiated": "2020-01-01T00:00:00Z",
           |"original": {
           |    "name" : "Mel",
           |    "description" : "Melodic deep house and techno songs",
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
           |    ],
           |    "source" : "Spotify"
           |  },
           |"durationMs": null,
           |"result": null,
           |"score": null
           |}]
           |""".stripMargin

      verifyJsonResponse(response, Status.Ok, Some(expected))
    }

    "return not found when optimization id is not recognized" in {
      when(playlistOptimizerMock.get(optimizationId)).thenReturn(IO.raiseError(OptimizationNotFound(optimizationId)))

      val request = Request[IO](uri = uri"/playlist-optimizations/607995e0-8e3a-11ea-bc55-0242ac130003", method = Method.GET)
      val response: IO[Response[IO]] = playlistController.routes.orNotFound.run(request)

      val expected =
        s"""
           |{"message": "optimization with id 607995e0-8e3a-11ea-bc55-0242ac130003 does not exist"}
           |""".stripMargin

      verifyJsonResponse(response, Status.NotFound, Some(expected))
    }

    "return bad request error if invalid json" in {
      val request = Request[IO](uri = uri"/playlist-optimizations", method = Method.POST).withEntity("{foo-bar}")
      val response: IO[Response[IO]] = playlistController.routes.orNotFound.run(request)

      verifyResponse[ErrorResponse](response, Status.BadRequest, Some(ErrorResponse("""Invalid message body: Could not decode JSON: "{foo-bar}"""")))
    }

    "return no content when deleting optimization" in {
      when(playlistOptimizerMock.delete(optimizationId)).thenReturn(IO.unit)

      val request = Request[IO](uri = uri"/playlist-optimizations/607995e0-8e3a-11ea-bc55-0242ac130003", method = Method.DELETE)
      val response: IO[Response[IO]] = playlistController.routes.orNotFound.run(request)

      verifyJsonResponse(response, Status.NoContent, None)
    }
  }
}

package io.kirill.playlistoptimizer.core.optimizer

import java.time.Instant
import java.util.UUID
import cats.effect.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import io.kirill.playlistoptimizer.core.ControllerSpec
import io.kirill.playlistoptimizer.core.common.controllers.Controller
import io.kirill.playlistoptimizer.core.common.errors.OptimizationNotFound
import io.kirill.playlistoptimizer.core.optimizer.OptimizationController.PlaylistOptimizationRequest
import io.kirill.playlistoptimizer.core.playlist.{Playlist, PlaylistBuilder, PlaylistView, Track}
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.circe.CirceEntityCodec.*
import org.mockito.ArgumentCaptor
import org.scalatest.OptionValues

class OptimizationControllerSpec extends ControllerSpec with OptionValues {
  import Controller.*

  val playlist          = PlaylistBuilder.playlist
  val shortenedPlaylist = playlist.copy(tracks = Vector(playlist.tracks.head))

  val optimizationId         = OptimizationId(UUID.fromString("607995e0-8e3a-11ea-bc55-0242ac130003"))
  val optimizationParameters = OptimizationParameters(100, 1000, 0.5, 0.2, 0.1, true)
  val optimization =
    Optimization(optimizationId, BigDecimal(0), optimizationParameters, shortenedPlaylist, Instant.parse("2020-01-01T00:00:00Z"))

  val userSessionCookie = RequestCookie("user-session", "user-session-id")
  val userSessionId     = UserSessionId("user-session-id")

  "A PlaylistController" should {

    "initiate optimization of a playlist" in {
      val playlistOptimizer  = mock[Optimizer[IO, Playlist, Track]]
      val playlistController = new OptimizationController[IO](playlistOptimizer)

      when(playlistOptimizer.optimize(any, any, any)).thenReturn(IO.pure(optimizationId))

      val requestBody = PlaylistOptimizationRequest(PlaylistView.from(shortenedPlaylist), optimizationParameters).asJson

      val request = Request[IO](uri = uri"/playlist-optimizations", method = Method.POST)
        .withEntity(requestBody)
        .addCookie(userSessionCookie)
      val response = playlistController.routesWithUserSession.orNotFound.run(request)

      val expected = s"""{"id": "${optimizationId.value}"}"""

      verifyJsonResponse(response, Status.Created, Some(expected), Map("user-session" -> "user-session-id"))
      verify(playlistOptimizer).optimize(userSessionId, shortenedPlaylist, optimizationParameters)
    }

    "get playlist optimization by id" in {
      val playlistOptimizer  = mock[Optimizer[IO, Playlist, Track]]
      val playlistController = new OptimizationController[IO](playlistOptimizer)

      when(playlistOptimizer.get(userSessionId, optimizationId)).thenReturn(IO.pure(optimization))

      val request = Request[IO](uri = uri"/playlist-optimizations/607995e0-8e3a-11ea-bc55-0242ac130003", method = Method.GET)
        .addCookie(userSessionCookie)
      val response = playlistController.routesWithUserSession.orNotFound.run(request)

      val expected =
        s"""
           |{
           |"id": "607995e0-8e3a-11ea-bc55-0242ac130003",
           |"progress": 0,
           |"parameters": { "populationSize": 100, "maxGen": 1000, "crossoverProbability": 0.5, "mutationProbability": 0.2, "elitismRatio": 0.1, "shuffle": true},
           |"dateInitiated": "2020-01-01T00:00:00Z",
           |"original": $shortenedPlaylistJson,
           |"durationMs": null,
           |"result": null,
           |"score": null
           |}
           |""".stripMargin

      verifyJsonResponse(response, Status.Ok, Some(expected), Map("user-session" -> "user-session-id"))
    }

    "return all optimizations" in {
      val playlistOptimizer  = mock[Optimizer[IO, Playlist, Track]]
      val playlistController = new OptimizationController[IO](playlistOptimizer)

      when(playlistOptimizer.getAll(userSessionId)).thenReturn(IO.pure(List(optimization)))

      val request  = Request[IO](uri = uri"/playlist-optimizations", method = Method.GET).addCookie(userSessionCookie)
      val response = playlistController.routesWithUserSession.orNotFound.run(request)

      val expected =
        s"""
           |[{
           |"id": "607995e0-8e3a-11ea-bc55-0242ac130003",
           |"progress": 0,
           |"parameters": { "populationSize": 100, "maxGen": 1000, "crossoverProbability": 0.5, "mutationProbability": 0.2, "elitismRatio": 0.1, "shuffle": true},
           |"dateInitiated": "2020-01-01T00:00:00Z",
           |"original": $shortenedPlaylistJson,
           |"durationMs": null,
           |"result": null,
           |"score": null
           |}]
           |""".stripMargin

      verifyJsonResponse(response, Status.Ok, Some(expected), Map("user-session" -> "user-session-id"))
    }

    "return not found when optimization id is not recognized" in {
      val playlistOptimizer  = mock[Optimizer[IO, Playlist, Track]]
      val playlistController = new OptimizationController[IO](playlistOptimizer)

      when(playlistOptimizer.get(userSessionId, optimizationId)).thenReturn(IO.raiseError(OptimizationNotFound(optimizationId)))

      val request = Request[IO](uri = uri"/playlist-optimizations/607995e0-8e3a-11ea-bc55-0242ac130003", method = Method.GET)
        .addCookie(userSessionCookie)
      val response = playlistController.routesWithUserSession.orNotFound.run(request)

      val expected =
        s"""
           |{"message": "optimization with id 607995e0-8e3a-11ea-bc55-0242ac130003 does not exist"}
           |""".stripMargin

      verifyJsonResponse(response, Status.NotFound, Some(expected), Map("user-session" -> "user-session-id"))
    }

    "return bad request error if invalid json" in {
      val playlistOptimizer  = mock[Optimizer[IO, Playlist, Track]]
      val playlistController = new OptimizationController[IO](playlistOptimizer)

      val request = Request[IO](uri = uri"/playlist-optimizations", method = Method.POST)
        .withEntity("{foo-bar}")
        .addCookie(userSessionCookie)
      val response = playlistController.routesWithUserSession.orNotFound.run(request)

      verifyResponse[ErrorResponse](
        response,
        Status.BadRequest,
        Some(ErrorResponse("""Invalid message body: Could not decode JSON: "{foo-bar}"""")),
        Map("user-session" -> "user-session-id")
      )
      verifyNoInteractions(playlistOptimizer)
    }

    "return no content when deleting optimization" in {
      val playlistOptimizer  = mock[Optimizer[IO, Playlist, Track]]
      val playlistController = new OptimizationController[IO](playlistOptimizer)

      when(playlistOptimizer.delete(userSessionId, optimizationId)).thenReturn(IO.unit)

      val request = Request[IO](uri = uri"/playlist-optimizations/607995e0-8e3a-11ea-bc55-0242ac130003", method = Method.DELETE)
        .addCookie(userSessionCookie)
      val response = playlistController.routesWithUserSession.orNotFound.run(request)

      verifyJsonResponse(response, Status.NoContent, None, Map("user-session" -> "user-session-id"))
      verify(playlistOptimizer).delete(UserSessionId("user-session-id"), optimizationId)
    }

    "set new user session cookie if none present" in {
      val playlistOptimizer  = mock[Optimizer[IO, Playlist, Track]]
      val playlistController = new OptimizationController[IO](playlistOptimizer)

      when(playlistOptimizer.delete(any[UserSessionId], any[OptimizationId])).thenReturn(IO.unit)

      val request  = Request[IO](uri = uri"/playlist-optimizations/607995e0-8e3a-11ea-bc55-0242ac130003", method = Method.DELETE)
      val response = playlistController.routesWithUserSession.orNotFound.run(request)

      val executedResponse = response.unsafeRunSync()

      executedResponse.status mustBe Status.NoContent
      val sessionCookie = executedResponse.cookies.find(_.name == "user-session").value
      verify(playlistOptimizer).delete(UserSessionId(sessionCookie.content), optimizationId)
    }
  }
}

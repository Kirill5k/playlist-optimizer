package io.kirill.playlistoptimizer.core.health

import cats.effect.IO
import io.kirill.playlistoptimizer.core.ControllerSpec
import org.http4s.*
import org.http4s.implicits.*

class HealthControllerSpec extends ControllerSpec {
  "A HealthController" when {

    "GET /health/status" should {
      "return 200 response" in {
        val controller = new HealthController[IO]

        val request  = Request[IO](uri = uri"/health/status", method = Method.GET)
        val response = controller.routes.orNotFound.run(request)

        verifyJsonResponse(response, Status.Ok, Some("""{"status": true}"""))
      }
    }
  }
}

package io.kirill.playlistoptimizer

import cats.effect._
import cats.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, StaticFile}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.syntax.kleisli._


object Application extends IOApp with Http4sDsl[IO] {
  override def run(args: List[String]): IO[ExitCode] =
    app.use(_ => IO.never).as(ExitCode.Success)

  val app: Resource[IO, Server[IO]] =
    for {
      blocker <- Blocker[IO]
      server <- BlazeServerBuilder[IO].bindHttp(8080).withHttpApp(Router(
          "" -> staticContentController(blocker),
          "api" -> restApiController
        ).orNotFound).resource
    } yield server


  def staticContentController(blocker: Blocker): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case req @ GET -> Root =>
        StaticFile.fromResource("static/index.html", blocker, Some(req)).getOrElseF(NotFound())
      case req @ GET -> "static" /: path =>
        StaticFile.fromResource(path.toString, blocker, Some(req)).getOrElseF(NotFound())
    }

  def restApiController: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "ping" =>
        Ok("pong")
    }
}

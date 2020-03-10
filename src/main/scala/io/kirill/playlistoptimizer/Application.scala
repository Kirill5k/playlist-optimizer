package io.kirill.playlistoptimizer

import cats.effect._
import cats.implicits._
import io.kirill.playlistoptimizer.controllers.{HomeController, SpotifyController}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.{HttpRoutes, StaticFile, Uri}
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
          "" -> HomeController[IO](blocker).routes,
          "spotify" -> SpotifyController[IO].routes
        ).orNotFound).resource
    } yield server
}

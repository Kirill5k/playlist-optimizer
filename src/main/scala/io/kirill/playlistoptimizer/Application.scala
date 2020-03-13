package io.kirill.playlistoptimizer

import cats.effect._
import cats.implicits._
import io.kirill.playlistoptimizer.configs.AppConfig
import io.kirill.playlistoptimizer.controllers.AppController
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.syntax.kleisli._
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.{NothingT, SttpBackend}
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend


object Application extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    app.use(_ => IO.never).as(ExitCode.Success)

  val client: Resource[IO, SttpBackend[IO, Nothing, NothingT]] =
    Resource.make(AsyncHttpClientCatsBackend[IO]())(_.close())

  val app: Resource[IO, Server[IO]] =
    for {
      blocker <- Blocker[IO]
      backend <- client
      config <- Resource.liftF(AppConfig.load(blocker))
      server <- BlazeServerBuilder[IO]
                  .bindHttp(config.server.port, config.server.hostname)
                  .withHttpApp(Router(
                    "" -> AppController.homeController(blocker).routes,
                    "spotify" -> AppController.spotifyController(config, backend).routes
                  ).orNotFound).resource
    } yield server
}

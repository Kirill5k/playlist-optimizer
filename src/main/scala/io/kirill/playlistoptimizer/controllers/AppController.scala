package io.kirill.playlistoptimizer.controllers

import cats.effect._
import io.kirill.playlistoptimizer.configs.{AppConfig, SpotifyConfig}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

trait AppController[F[_]] extends Http4sDsl[F] {
  def routes: HttpRoutes[F]
}

object AppController {

  def homeController(blocker: Blocker)(implicit cs: ContextShift[IO]): AppController[IO] =
    new HomeController[IO](blocker)

  def spotifyController(config: AppConfig)(implicit cs: ContextShift[IO]): AppController[IO] = {
    implicit val sc: SpotifyConfig = config.spotify
    new SpotifyController[IO]
  }
}

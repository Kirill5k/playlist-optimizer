package io.kirill.playlistoptimizer.controllers

import cats.effect._
import io.kirill.playlistoptimizer.configs.{AppConfig, SpotifyConfig}
import io.kirill.playlistoptimizer.spotify.SpotifyPlaylistController
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import sttp.client.{NothingT, SttpBackend}

trait AppController[F[_]] extends Http4sDsl[F] {
  def routes(implicit C: ContextShift[F], S: Sync[F]): HttpRoutes[F]
}

object AppController {

  def homeController(blocker: Blocker)(implicit cs: ContextShift[IO]): AppController[IO] =
    new HomeController[IO](blocker)

  def spotifyController(config: AppConfig)(implicit cs: ContextShift[IO], B: SttpBackend[IO, Nothing, NothingT]): AppController[IO] = {
    implicit val sc: SpotifyConfig = config.spotify
    new SpotifyPlaylistController
  }
}

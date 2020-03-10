package io.kirill.playlistoptimizer.controllers

import cats.effect._
import cats.implicits._
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import org.http4s.{HttpRoutes, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location

class SpotifyController[F[_]: Sync](implicit config: SpotifyConfig, cs: ContextShift[F]) extends AppController[F] {

  override def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "ping" => Ok("spotify-pong")
      case GET -> Root / "login" => TemporaryRedirect(Location(Uri.uri("http://google.com")))
    }
}

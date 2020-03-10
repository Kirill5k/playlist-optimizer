package io.kirill.playlistoptimizer.spotify

import cats.effect._
import cats.implicits._
import org.http4s.{HttpRoutes, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location

class SpotifyController[F[_]](implicit F: Effect[F], cs: ContextShift[F]) extends Http4sDsl[F] {

  def routes(implicit timer: Timer[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "ping" => Ok("spotify-pong")
      case GET -> Root / "login" => TemporaryRedirect(Location(Uri.uri("http://google.com")))
    }
}

object SpotifyController {
  def apply[F[_]: Effect: ContextShift]: SpotifyController[F] = new SpotifyController[F]
}

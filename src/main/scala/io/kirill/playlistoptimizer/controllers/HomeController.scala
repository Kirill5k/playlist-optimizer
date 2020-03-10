package io.kirill.playlistoptimizer.controllers

import cats.effect._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, StaticFile, Uri}

class HomeController[F[_]](blocker: Blocker)(implicit F: Effect[F], cs: ContextShift[F]) extends Http4sDsl[F] {

  def routes(implicit timer: Timer[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ GET -> Root =>
        StaticFile.fromResource("static/index.html", blocker, Some(req)).getOrElseF(NotFound())
      case req @ GET -> "static" /: path =>
        StaticFile.fromResource(path.toString, blocker, Some(req)).getOrElseF(NotFound())
    }
}

object HomeController {
  def apply[F[_]: Effect: ContextShift](blocker: Blocker): HomeController[F] = new HomeController[F](blocker)
}



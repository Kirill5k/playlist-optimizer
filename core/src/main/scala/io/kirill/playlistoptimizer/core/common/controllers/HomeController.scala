package io.kirill.playlistoptimizer.core.common.controllers

import cats.effect._
import io.chrisdavenport.log4cats.Logger
import org.http4s.{HttpRoutes, StaticFile}

private[controllers] class HomeController[F[_]](blocker: Blocker) extends AppController[F] {

  override def routes(implicit cs: ContextShift[F], s: Sync[F], l: Logger[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ GET -> Root =>
        StaticFile.fromResource("static/index.html", blocker, Some(req)).getOrElseF(NotFound())
      case req @ GET -> "static" /: path =>
        StaticFile.fromResource(path.toString, blocker, Some(req)).getOrElseF(NotFound())
    }
}



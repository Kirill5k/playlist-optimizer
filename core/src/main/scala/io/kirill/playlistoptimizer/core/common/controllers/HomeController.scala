package io.kirill.playlistoptimizer.core.common.controllers

import cats.effect._
import com.typesafe.scalalogging.Logger
import org.http4s.{HttpRoutes, StaticFile}

class HomeController[F[_]](blocker: Blocker) extends AppController[F] {

  protected val logger = Logger[HomeController[F]]

  override def routes(implicit cs: ContextShift[F], s: Sync[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ GET -> Root =>
        StaticFile.fromResource("static/index.html", blocker, Some(req)).getOrElseF(NotFound())
      case req @ GET -> "static" /: path =>
        StaticFile.fromResource(path.toString, blocker, Some(req)).getOrElseF(NotFound())
    }
}


package io.kirill.playlistoptimizer.core.common.controllers

import cats.effect._
import io.chrisdavenport.log4cats.Logger
import org.http4s.{HttpRoutes, StaticFile}
import java.io.File

private[controllers] class HomeController[F[_]](blocker: Blocker) extends AppController[F] {

  override def routes(implicit cs: ContextShift[F], s: Sync[F], l: Logger[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ GET -> Root / path if List(".js", ".css", ".map", ".html", ".webm").exists(path.endsWith) =>
        StaticFile.fromFile(new File(s"static/$path"), blocker, Some(req)).getOrElseF(NotFound())
    }

}



package io.kirill.playlistoptimizer.core.common.controllers

import cats.effect._
import io.chrisdavenport.log4cats.Logger
import org.http4s.{HttpRoutes, StaticFile}
import java.io.File

private[controllers] class HomeController[F[_]](blocker: Blocker) extends AppController[F] {

  private val expectedFiles = List(".txt", ".ico", ".svg", ".png", ".json", ".js", ".css", ".map", ".html", ".webm")

  override def routes(implicit cs: ContextShift[F], s: Sync[F], l: Logger[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ GET -> Root  =>
        StaticFile.fromFile(new File(s"static/index.html"), blocker, Some(req)).getOrElseF(NotFound())
      case req @ GET -> path if expectedFiles.exists(path.toList.last.endsWith) =>
        StaticFile.fromFile(new File(s"static/${path.toList.mkString("/")}"), blocker, Some(req)).getOrElseF(NotFound())
    }

}



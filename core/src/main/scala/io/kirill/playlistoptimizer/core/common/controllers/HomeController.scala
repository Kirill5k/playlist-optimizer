package io.kirill.playlistoptimizer.core.common.controllers

import cats.effect._
import org.http4s.{HttpRoutes, StaticFile}
import java.io.File

private[controllers] class HomeController[F[_]: ContextShift: Sync](blocker: Blocker) extends Controller[F] {

  private val expectedFiles = List(".txt", ".ico", ".svg", ".png", ".json", ".js", ".css", ".map", ".html", ".webm")

  override def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ GET -> Root  =>
        StaticFile.fromFile(new File(s"static/index.html"), blocker, Some(req)).getOrElseF(NotFound())
      case req @ GET -> path if expectedFiles.exists(path.toList.last.endsWith) =>
        StaticFile.fromFile(new File(s"static/${path.toList.mkString("/")}"), blocker, Some(req)).getOrElseF(NotFound())
    }

}

object HomeController {

  def make[F[_]: ContextShift: Sync](blocker: Blocker): F[Controller[F]] =
    Sync[F].delay(new HomeController[F](blocker))
}



package io.kirill.playlistoptimizer.playlist

import cats.implicits._
import cats.effect.{ContextShift, Sync}
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.controllers.AppController
import org.http4s.circe._
import org.http4s.{EntityDecoder, HttpRoutes}


trait PlaylistController[F[_]] extends AppController[F] {
  protected implicit def decoder: EntityDecoder[F, Playlist]

  protected def playlistService: PlaylistService[F]

  override def routes(implicit C: ContextShift[F], S: Sync[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "playlists" =>
        for {
          playlists <- playlistService.getAll
          resp <- Ok(playlists.asJson)
        } yield resp
      case req @ POST -> Root / "playlists" =>
        for {
          pl <- req.as[Playlist]
          _ <- playlistService.save(pl)
          resp <- Created()
        } yield resp
      case req @ POST -> Root / "playlists" / "optimize" =>
        for {
          pl <- req.as[Playlist]
          optPl <- C.shift *> playlistService.optimize(pl)
          resp <- Ok(optPl.asJson)
        } yield resp
    }
}

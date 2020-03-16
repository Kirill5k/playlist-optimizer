package io.kirill.playlistoptimizer.playlist

import java.time.LocalDate
import java.util.concurrent.TimeUnit

import cats.implicits._
import cats.effect.{ContextShift, Sync}
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.common.controllers.AppController
import org.http4s.circe._
import org.http4s.{EntityDecoder, HttpRoutes, Response}

import scala.concurrent.duration._
import scala.language.postfixOps

trait PlaylistController[F[_]] extends AppController[F] {
  import PlaylistController._

  protected def playlistService: PlaylistService[F]

  override def routes(implicit cs: ContextShift[F], s: Sync[F]): HttpRoutes[F] = {
    implicit val decoder: EntityDecoder[F, PlaylistView] = jsonOf[F, PlaylistView]
    HttpRoutes.of[F] {
      case GET -> Root / "playlists" => withErrorHandling {
        for {
          _ <- s.delay(logger.info("get all playlists"))
          playlists <- playlistService.getAll
          views = playlists.map(PlaylistView.from)
          resp <- Ok(views.asJson)
        } yield resp
      }
      case req @ POST -> Root / "playlists" => withErrorHandling {
        for {
          view <- req.as[PlaylistView]
          _ <- s.delay(logger.info(s"save playlist ${view.name}"))
          _ <- playlistService.save(view.toDomain)
          resp <- Created()
        } yield resp
      }
      case req @ POST -> Root / "playlists" / "optimize" => withErrorHandling {
        for {
          view <- req.as[PlaylistView]
          _ <- s.delay(logger.info(s"optimize playlist ${view.name}"))
          optimizedPlaylist <- cs.shift *> playlistService.optimize(view.toDomain)
          view = PlaylistView.from(optimizedPlaylist)
          resp <- Ok(view.asJson)
        } yield resp
      }
    }
  }
}

object PlaylistController {
  final case class TrackView(
                              name: String,
                              artists: Seq[String],
                              releaseName: Option[String],
                              releaseDate: Option[LocalDate],
                              releaseType: Option[String],
                              tempo: Double,
                              duration: Double,
                              key: Int,
                              mode: Int,
                              uri: String,
                              url: Option[String]
                            ) {
    def toDomain: Track =
      Track(SongDetails(name, artists, releaseName, releaseDate, releaseType), AudioDetails(tempo, duration seconds, Key(key, mode)), SourceDetails(uri, url))
  }

  object TrackView {
    def from(track: Track): TrackView = track match {
      case Track(SongDetails(name, artists, releaseName, releaseDate, releaseType), AudioDetails(tempo, duration, key), SourceDetails(uri, url)) =>
        TrackView(name, artists, releaseName, releaseDate, releaseType, tempo, duration.toUnit(TimeUnit.SECONDS), key.number, key.mode.number, uri, url)
    }
  }

  final case class PlaylistView(name: String, description: Option[String], source: String, tracks: Seq[TrackView]) {
    def toDomain: Playlist = Playlist(name, description, PlaylistSource(source), tracks.map(_.toDomain))
  }

  object PlaylistView {
    def from(playlist: Playlist): PlaylistView =
      PlaylistView(playlist.name, playlist.description, playlist.source.toString, playlist.tracks.map(TrackView.from))
  }
}

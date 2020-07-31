package io.kirill.playlistoptimizer.core.playlist

import java.time.{Instant, LocalDate}
import java.util.concurrent.TimeUnit

import cats.implicits._
import cats.effect.{ContextShift, Sync}
import io.chrisdavenport.log4cats.Logger
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.common.controllers.AppController
import io.kirill.playlistoptimizer.core.common.json._
import org.http4s.circe._
import org.http4s.{EntityDecoder, HttpRoutes}

import scala.concurrent.duration._

trait PlaylistController[F[_]] extends AppController[F] {
  import PlaylistController._

  protected def playlistService: PlaylistService[F]

  override def routes(implicit cs: ContextShift[F], s: Sync[F], l: Logger[F]): HttpRoutes[F] = {
    implicit val decoder: EntityDecoder[F, PlaylistView] = jsonOf[F, PlaylistView]
    HttpRoutes.of[F] {
      case GET -> Root / "playlists" =>
        withErrorHandling {
          for {
            _         <- l.info("get all playlists")
            playlists <- playlistService.getAll
            views = playlists.map(PlaylistView.from)
            resp <- Ok(views.asJson)
          } yield resp
        }
      case req @ POST -> Root / "playlists" =>
        withErrorHandling {
          for {
            view <- req.as[PlaylistView]
            _    <- l.info(s"save playlist ${view.name}")
            _    <- playlistService.save(view.toDomain)
            resp <- Created()
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
      Track(
        SongDetails(name, artists, releaseName, releaseDate, releaseType),
        AudioDetails(tempo, duration.seconds, Key(key, mode)),
        SourceDetails(uri, url)
      )
  }

  object TrackView {
    def from(track: Track): TrackView = track match {
      case Track(
          SongDetails(name, artists, releaseName, releaseDate, releaseType),
          AudioDetails(tempo, duration, key),
          SourceDetails(uri, url)
          ) =>
        TrackView(
          name,
          artists,
          releaseName,
          releaseDate,
          releaseType,
          tempo,
          duration.toUnit(TimeUnit.SECONDS),
          key.number,
          key.mode.number,
          uri,
          url
        )
    }
  }

  final case class PlaylistView(
      name: String,
      description: Option[String],
      tracks: Seq[TrackView],
      source: String
  ) {
    def toDomain: Playlist = Playlist(name, description, tracks.map(_.toDomain), PlaylistSource(source))
  }

  object PlaylistView {
    def from(playlist: Playlist): PlaylistView =
      PlaylistView(
        playlist.name,
        playlist.description,
        playlist.tracks.map(TrackView.from),
        playlist.source.toString
      )
  }
}

package io.kirill.playlistoptimizer.core.playlist

import java.time.LocalDate
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

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
  def toDomain: Playlist = Playlist(name, description, tracks.map(_.toDomain).toVector, PlaylistSource(source))
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

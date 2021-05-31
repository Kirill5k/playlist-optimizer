package io.kirill.playlistoptimizer.core.playlist

import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

final case class TrackView(
    name: String,
    artists: List[String],
    release: Release,
    artwork: Option[String],
    tempo: Double,
    duration: Double,
    key: Int,
    mode: Int,
    danceability: Double,
    energy: Double,
    uri: String,
    url: Option[String]
) {
  def toDomain: Track =
    Track(
      SongDetails(name, artists, release, artwork),
      AudioDetails(tempo, duration.seconds, Key(key, mode), danceability, energy),
      SourceDetails(uri, url)
    )
}

object TrackView {
  def from(track: Track): TrackView = track match {
    case Track(
        SongDetails(name, artists, release, artwork),
        AudioDetails(tempo, duration, key, danceability, energy),
        SourceDetails(uri, url)
        ) =>
      TrackView(
        name,
        artists,
        release,
        artwork,
        tempo,
        duration.toUnit(TimeUnit.SECONDS),
        key.number,
        key.mode.number,
        danceability,
        energy,
        uri,
        url
      )
  }
}

final case class PlaylistView(
    name: String,
    description: Option[String],
    tracks: List[TrackView],
    source: String
) {
  def toDomain: Playlist = Playlist(name, description, tracks.map(_.toDomain).toVector, PlaylistSource(source))
}

object PlaylistView {
  def from(playlist: Playlist): PlaylistView =
    PlaylistView(
      playlist.name,
      playlist.description,
      playlist.tracks.map(TrackView.from).toList,
      playlist.source.toString
    )
}

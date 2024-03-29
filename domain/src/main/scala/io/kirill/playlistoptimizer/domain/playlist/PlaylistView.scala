package io.kirill.playlistoptimizer.domain.playlist

import io.circe.Codec
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*

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
) derives Codec.AsObject {
  def toDomain: Track =
    Track(
      SongDetails(name, artists, release, artwork),
      AudioDetails(tempo, duration.seconds, Key(key, mode), danceability, energy),
      SourceDetails(uri, url)
    )
}

object TrackView {
  def from(track: Track): TrackView = track match
    case Track(song, audio, source) =>
      TrackView(
        song.name,
        song.artists,
        song.release,
        song.artwork,
        audio.tempo,
        audio.duration.toUnit(TimeUnit.SECONDS),
        audio.key.number,
        audio.key.mode.number,
        audio.danceability,
        audio.energy,
        source.uri,
        source.url
      )
}

final case class PlaylistView(
    name: String,
    description: Option[String],
    tracks: List[TrackView],
    source: String
) derives Codec.AsObject {
  def toDomain: Playlist =
    Playlist(name, description, tracks.map(_.toDomain).toVector, PlaylistSource.valueOf(source))
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

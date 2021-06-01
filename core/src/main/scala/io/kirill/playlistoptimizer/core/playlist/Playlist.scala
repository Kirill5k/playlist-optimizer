package io.kirill.playlistoptimizer.core.playlist

import java.time.LocalDate
import io.kirill.playlistoptimizer.core.common.errors.UnexpectedPlaylistSource
import io.kirill.playlistoptimizer.core.optimizer.algorithms.Optimizable

import scala.concurrent.duration.FiniteDuration

sealed trait PlaylistSource
object PlaylistSource {
  final case object Spotify extends PlaylistSource

  def apply(source: String): PlaylistSource = source.toLowerCase match {
    case "spotify" => Spotify
    case _         => throw UnexpectedPlaylistSource(source)
  }
}

final case class Release(
    name: String,
    kind: String,
    date: Option[LocalDate],
    uid: Option[String]
)

final case class SongDetails(
    name: String,
    artists: List[String],
    release: Release,
    artwork: Option[String]
)

final case class AudioDetails(
    tempo: Double,
    duration: FiniteDuration,
    key: Key,
    danceability: Double,
    energy: Double
)

final case class SourceDetails(
    uri: String,
    url: Option[String]
)

final case class Track(
    song: SongDetails,
    audio: AudioDetails,
    source: SourceDetails
)

final case class Playlist(
    name: String,
    description: Option[String],
    tracks: IndexedSeq[Track],
    source: PlaylistSource
) extends Optimizable[Track] {
  override val repr: Array[Track] = tracks.toArray[Track]
}

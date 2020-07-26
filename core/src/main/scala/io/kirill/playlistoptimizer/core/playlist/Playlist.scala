package io.kirill.playlistoptimizer.core.playlist

import java.time.LocalDate

import io.kirill.playlistoptimizer.core.common.errors.UnexpectedPlaylistSource

import scala.concurrent.duration.FiniteDuration

sealed trait PlaylistSource
object PlaylistSource {
  final case object Spotify extends PlaylistSource

  def apply(source: String): PlaylistSource = source match {
    case "Spotify" => Spotify
    case _         => throw UnexpectedPlaylistSource(source)
  }
}

final case class SongDetails(
    name: String,
    artists: Seq[String],
    releaseName: Option[String],
    releaseDate: Option[LocalDate],
    releaseType: Option[String]
)

final case class AudioDetails(
    tempo: Double,
    duration: FiniteDuration,
    key: Key
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
    tracks: Seq[Track],
    source: PlaylistSource
)
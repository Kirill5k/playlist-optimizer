package io.kirill.playlistoptimizer.core.playlist

import java.time.LocalDate
import io.kirill.playlistoptimizer.core.common.errors.UnexpectedPlaylistSource

import scala.concurrent.duration.FiniteDuration

enum PlaylistSource:
  case Spotify

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
)

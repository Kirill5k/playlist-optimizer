package io.kirill.playlistoptimizer.playlist

import java.time.LocalDate

import io.kirill.playlistoptimizer.playlist.PlaylistSource.PlaylistSource

import scala.concurrent.duration.Duration

object PlaylistSource extends Enumeration {
  type PlaylistSource = Value
  val Spotify = Value
}

final case class SongDetails(name: String, artists: Seq[String], releaseName: Option[String], releaseDate: Option[LocalDate], releaseType: Option[String])

final case class AudioDetails(tempo: Double, duration: Duration, key: Key)

final case class SourceDetails(uri: String, url: Option[String])

final case class Track(song: SongDetails, audio: AudioDetails, source: SourceDetails)

final case class Playlist(name: String, description: Option[String], source: PlaylistSource, tracks: Seq[Track])
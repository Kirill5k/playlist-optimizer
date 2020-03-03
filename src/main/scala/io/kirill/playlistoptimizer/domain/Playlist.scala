package io.kirill.playlistoptimizer.domain

import io.kirill.playlistoptimizer.domain.PlaylistSource.PlaylistSource

import scala.concurrent.duration.Duration

object PlaylistSource extends Enumeration {
  type PlaylistSource = Value
  val Spotify = Value
}

final case class SongDetails(name: String, artists: Seq[String], album: Option[String])

final case class AudioDetails(tempo: Double, duration: Duration, key: Key)

final case class Track(song: SongDetails, audio: AudioDetails)

final case class Playlist(name: String, description: Option[String], source: PlaylistSource, tracks: IndexedSeq[Track])
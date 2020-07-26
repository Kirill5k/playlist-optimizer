package io.kirill.playlistoptimizer.playlist

import java.time.LocalDate

import scala.concurrent.duration.Duration

sealed trait PlaylistSource
object PlaylistSource {
  final case object Spotify extends PlaylistSource

  def apply(source: String): PlaylistSource = source match {
    case "Spotify" => Spotify
    case _ => throw new IllegalArgumentException(s"unrecognized playlist source $source")
  }
}


final case class SongDetails(name: String, artists: Seq[String], releaseName: Option[String], releaseDate: Option[LocalDate], releaseType: Option[String])

final case class AudioDetails(tempo: Double, duration: Duration, key: Key)

final case class SourceDetails(uri: String, url: Option[String])

final case class Track(song: SongDetails, audio: AudioDetails, source: SourceDetails)

final case class Playlist(name: String, description: Option[String], source: PlaylistSource, tracks: Seq[Track])
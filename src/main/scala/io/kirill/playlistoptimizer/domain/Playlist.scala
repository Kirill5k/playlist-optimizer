package io.kirill.playlistoptimizer.domain

final case class TrackDetails(name: String, artists: Seq[String], album: Option[String])

final case class AudioDetails(tempo: Double, duration: Int, key: Key)

final case class Track(details: TrackDetails, audio: AudioDetails)

final case class Playlist(name: String, source: String, tracks: IndexedSeq[Song])
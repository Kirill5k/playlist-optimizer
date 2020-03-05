package io.kirill.playlistoptimizer.clients.spotify

import java.util.concurrent.TimeUnit

import io.kirill.playlistoptimizer.domain.{AudioDetails, Key, Mode, SongDetails, Track}

import scala.concurrent.duration.Duration

object SpotifyMapper {

  val toDomain: (SpotifyResponse.PlaylistTrack, SpotifyResponse.AudioAnalysisTrack) => Track = (song, audio) => {
    val songDetails = SongDetails(
      song.name,
      song.artists.map(artist => artist.name),
      Some(song.album.name).filter(_.nonEmpty)
    )
    val audioDetails = AudioDetails(
      audio.tempo,
      Duration(audio.duration, TimeUnit.SECONDS),
      Key(audio.key+1, Mode(audio.mode))
    )
    Track(songDetails, audioDetails)
  }
}
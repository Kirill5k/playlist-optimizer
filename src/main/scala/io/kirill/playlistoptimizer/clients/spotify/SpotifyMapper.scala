package io.kirill.playlistoptimizer.clients.spotify

import java.time.LocalDate
import java.util.concurrent.TimeUnit

import io.kirill.playlistoptimizer.domain.{AudioDetails, Key, Mode, SongDetails, SourceDetails, Track}

import scala.concurrent.duration.Duration

object SpotifyMapper {

  def toDomain(song: SpotifyResponse.PlaylistTrack, audio: SpotifyResponse.AudioAnalysisTrack): Track =
    Track(song.toSongDetails, audio.toAudioDetails, song.toSourceDetails)

  def toDomain(song: SpotifyResponse.PlaylistTrack, audio: SpotifyResponse.SpotifyAudioFeaturesResponse): Track =
    Track(song.toSongDetails, audio.toDomain, song.toSourceDetails)


  implicit class PlaylistTrackSyntax(val track: SpotifyResponse.PlaylistTrack) extends AnyVal {
    def toSongDetails: SongDetails =
      SongDetails(
        track.name,
        track.artists.map(_.name),
        Some(track.album.name).filter(_.nonEmpty),
        track.album.release_date_precision.filter(_ == "day").flatMap(_ => track.album.release_date.map(LocalDate.parse)),
        Some(track.album.album_type)
      )

    def toSourceDetails: SourceDetails =
      SourceDetails(track.uri, Some(track.external_urls.spotify))
  }

  implicit class AudioAnalysisTrackSyntax(val track: SpotifyResponse.AudioAnalysisTrack) extends AnyVal {
    def toAudioDetails: AudioDetails =
      AudioDetails(
        track.tempo,
        Duration(track.duration, TimeUnit.SECONDS),
        Key(track.key+1, Mode(track.mode))
      )
  }

  implicit class AudioFeaturesSyntax(val features: SpotifyResponse.SpotifyAudioFeaturesResponse) extends AnyVal {
    def toDomain: AudioDetails =
      AudioDetails(
        features.tempo,
        Duration(features.duration_ms, TimeUnit.MILLISECONDS),
        Key(features.key+1, Mode(features.mode))
      )
  }
}

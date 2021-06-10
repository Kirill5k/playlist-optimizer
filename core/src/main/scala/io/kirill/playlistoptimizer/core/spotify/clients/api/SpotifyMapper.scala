package io.kirill.playlistoptimizer.core.spotify.clients.api

import java.time.LocalDate
import java.util.concurrent.TimeUnit

import io.kirill.playlistoptimizer.core.playlist.*

import scala.concurrent.duration.FiniteDuration

object SpotifyMapper {

  def toDomain(song: responses.PlaylistTrack, audio: responses.SpotifyAudioFeaturesResponse): Track =
    Track(song.toSongDetails, audio.toDomain, song.toSourceDetails)

  implicit class PlaylistTrackSyntax(private val track: responses.PlaylistTrack) extends AnyVal {
    def toSongDetails: SongDetails =
      SongDetails(
        name = track.name,
        artists = track.artists.map(_.name),
        release = Release(
          name = track.album.name,
          kind = track.album.album_type,
          date = track.album.release_date_precision.filter(_ == "day").flatMap(_ => track.album.release_date.map(LocalDate.parse)),
          uid = track.external_ids.isrc
        ),
        artwork = track.album.images.maxByOption(_.height).map(_.url)
      )

    def toSourceDetails: SourceDetails =
      SourceDetails(track.uri, Some(track.external_urls.spotify))
  }

  implicit class AudioFeaturesSyntax(private val features: responses.SpotifyAudioFeaturesResponse) extends AnyVal {
    def toDomain: AudioDetails =
      AudioDetails(
        features.tempo,
        FiniteDuration(features.duration_ms.longValue, TimeUnit.MILLISECONDS),
        Key(features.key+1, features.mode),
        features.danceability,
        features.energy
      )
  }

  def sanitiseTrackSearchQuery(name: String): String =
    name
      .replaceAll("[-]", "")
      .replaceAll("[():]", "")
      .replaceAll("(?i)(original mix|remix)", "")
      .replaceAll(" +", " ")
      .trim
}

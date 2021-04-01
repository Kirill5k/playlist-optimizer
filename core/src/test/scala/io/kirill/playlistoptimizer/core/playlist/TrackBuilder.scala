package io.kirill.playlistoptimizer.core.playlist

import java.util.concurrent.TimeUnit

import io.kirill.playlistoptimizer.core.playlist.Key.EMajor

import scala.concurrent.duration.Duration

object TrackBuilder {

  def track(
      name: String,
      key: Key = EMajor,
      danceability: Double = 0.807,
      energy: Double = 0.613,
      artist: String = "Artist"
  ): Track =
    Track(
      SongDetails(name, List(artist), Release("The Album", "LP", None, None), None),
      AudioDetails(140, Duration(3, TimeUnit.MINUTES), key, danceability, energy),
      SourceDetails(s"file://$artist-$name", None)
    )
}

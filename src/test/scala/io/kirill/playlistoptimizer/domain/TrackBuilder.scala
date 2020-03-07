package io.kirill.playlistoptimizer.domain

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration

object TrackBuilder {

  def track(name: String, key: Key, artist: String = "Artist"): Track = {
    val details = SongDetails(name, List(artist), None, None, None)
    val audio = AudioDetails(140, Duration(3, TimeUnit.MINUTES), key)
    Track(details, audio)
  }
}

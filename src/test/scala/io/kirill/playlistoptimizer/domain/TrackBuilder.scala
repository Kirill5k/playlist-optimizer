package io.kirill.playlistoptimizer.domain

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration

object TrackBuilder {

  def track(name: String, key: Key, artist: String = "Artist"): Track = {
    val details = TrackDetails(name, List(artist), None)
    val audio = AudioDetails(140, Duration(3, TimeUnit.MINUTES), key)
    Track(details, audio)
  }
}

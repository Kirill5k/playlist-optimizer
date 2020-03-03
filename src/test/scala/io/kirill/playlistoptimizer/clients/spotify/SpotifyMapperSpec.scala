package io.kirill.playlistoptimizer.clients.spotify

import java.util.concurrent.TimeUnit

import io.kirill.playlistoptimizer.clients.spotify.SpotifyResponse.{AudioAnalysisTrack, PlaylistTrack, PlaylistTrackAlbum, PlaylistTrackArtist}
import io.kirill.playlistoptimizer.domain.Key.GMinor
import io.kirill.playlistoptimizer.domain.{AudioDetails, SongDetails, Track}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.Duration

class SpotifyMapperSpec extends AnyWordSpec with Matchers {

  "A SpotifyMapper" should {

    "map song and audio details to a song" in {
      val song = PlaylistTrack(
        "track-id",
        "I'm Not The Only One - Radio Edit",
        PlaylistTrackAlbum("5GWoXPsTQylMuaZ84PC563", "single", "I'm Not The Only One"),
        List(PlaylistTrackArtist("2wY79sveU1sp5g7SokKOiI", "Sam Smith"), PlaylistTrackArtist("2wY79sveU1ABCg7SokKOiI", "Bruno Mars")),
        45.0
      )
      val audio = AudioAnalysisTrack(255.34898, 98.002, 5, 0)

      val track = SpotifyMapper.toDomain(song, audio)

      track must be (Track(
        SongDetails("I'm Not The Only One - Radio Edit", List("Sam Smith", "Bruno Mars"), Some("I'm Not The Only One")),
        AudioDetails(98.002, Duration(255.34898, TimeUnit.SECONDS), GMinor)
      ))
    }
  }
}

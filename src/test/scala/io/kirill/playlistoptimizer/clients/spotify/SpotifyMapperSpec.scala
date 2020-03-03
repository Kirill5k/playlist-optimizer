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
    val song = PlaylistTrack(
      "track-id",
      "I'm Not The Only One - Radio Edit",
      PlaylistTrackAlbum("5GWoXPsTQylMuaZ84PC563", "single", "I'm Not The Only One"),
      List(PlaylistTrackArtist("2wY79sveU1sp5g7SokKOiI", "Sam Smith"), PlaylistTrackArtist("2wY79sveU1ABCg7SokKOiI", "Bruno Mars")),
      45.0
    )
    val audio = AudioAnalysisTrack(255.34898, 98.002, 5, 0)

    "map song and audio details to a song" in {
      val track = SpotifyMapper.toDomain(song, audio)

      track must be (Track(
        SongDetails("I'm Not The Only One - Radio Edit", List("Sam Smith", "Bruno Mars"), Some("I'm Not The Only One")),
        AudioDetails(98.002, Duration(255.34898, TimeUnit.SECONDS), GMinor)
      ))
    }

    "throw exception when invalid key" in {
      the [IllegalArgumentException] thrownBy {
        SpotifyMapper.toDomain(song, audio.copy(key = 15))
      } must have message "couldn't find key with number 16 and mode Minor"
    }

    "throw exception when invalid mode" in {
      the [IllegalArgumentException] thrownBy {
        SpotifyMapper.toDomain(song, audio.copy(mode = 2))
      } must have message "couldn't find mode with number 2"
    }

    "work with tuples" in {
      val details = (song, audio)

      val track = SpotifyMapper.toDomain.tupled(details)

      track must be (Track(
        SongDetails("I'm Not The Only One - Radio Edit", List("Sam Smith", "Bruno Mars"), Some("I'm Not The Only One")),
        AudioDetails(98.002, Duration(255.34898, TimeUnit.SECONDS), GMinor)
      ))
    }
  }
}

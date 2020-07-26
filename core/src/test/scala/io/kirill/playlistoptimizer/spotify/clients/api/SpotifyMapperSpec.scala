package io.kirill.playlistoptimizer.spotify.clients.api

import java.time.LocalDate
import java.util.concurrent.TimeUnit

import io.kirill.playlistoptimizer.playlist.Key.GMinor
import io.kirill.playlistoptimizer.playlist.{AudioDetails, SongDetails, SourceDetails, Track}
import io.kirill.playlistoptimizer.spotify.clients.api.SpotifyResponse._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.Duration

class SpotifyMapperSpec extends AnyWordSpec with Matchers {

  "A SpotifyMapper" should {
    val song = PlaylistTrack(
      "track-id",
      "I'm Not The Only One - Radio Edit",
      PlaylistTrackAlbum("5GWoXPsTQylMuaZ84PC563", "Single", "I'm Not The Only One", Some("2012-10-10"), Some("day")),
      List(PlaylistTrackArtist("2wY79sveU1sp5g7SokKOiI", "Sam Smith"), PlaylistTrackArtist("2wY79sveU1ABCg7SokKOiI", "Bruno Mars")),
      45.0,
      "spotify:track:track-id",
      PlaylistTrackUrls("http://spotify.com/tracks/track-id")
    )
    val audioAnalysis = AudioAnalysisTrack(255.34898, 98.002, 5, 0)

    val audioFeatures = SpotifyAudioFeaturesResponse(5, 0, 255348.98, 98.002)

    "map song and audioAnalysis details to a song" in {
      val track = SpotifyMapper.toDomain(song, audioAnalysis)

      track must be (Track(
        SongDetails("I'm Not The Only One - Radio Edit", List("Sam Smith", "Bruno Mars"), Some("I'm Not The Only One"), Some(LocalDate.of(2012, 10 ,10)), Some("Single")),
        AudioDetails(98.002, Duration(255.34898, TimeUnit.SECONDS), GMinor),
        SourceDetails("spotify:track:track-id", Some("http://spotify.com/tracks/track-id"))
      ))
    }

    "map song and audioFeatures details to a song" in {
      val track = SpotifyMapper.toDomain(song, audioFeatures)

      track must be (Track(
        SongDetails("I'm Not The Only One - Radio Edit", List("Sam Smith", "Bruno Mars"), Some("I'm Not The Only One"), Some(LocalDate.of(2012, 10 ,10)), Some("Single")),
        AudioDetails(98.002, Duration(255.34898, TimeUnit.SECONDS), GMinor),
        SourceDetails("spotify:track:track-id", Some("http://spotify.com/tracks/track-id"))
      ))
    }


    "throw exception when invalid key" in {
      the [IllegalArgumentException] thrownBy {
        SpotifyMapper.toDomain(song, audioAnalysis.copy(key = 15))
      } must have message "couldn't find key with number 16 and mode Minor"
    }

    "throw exception when invalid mode" in {
      the [IllegalArgumentException] thrownBy {
        SpotifyMapper.toDomain(song, audioAnalysis.copy(mode = 2))
      } must have message "couldn't find mode with number 2"
    }
  }
}

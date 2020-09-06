package io.kirill.playlistoptimizer.core.spotify.clients.api

import java.time.LocalDate
import java.util.concurrent.TimeUnit

import io.kirill.playlistoptimizer.core.common.errors.{InvalidKey, InvalidMode}
import io.kirill.playlistoptimizer.core.playlist.Key.GMinor
import io.kirill.playlistoptimizer.core.playlist.{AudioDetails, SongDetails, SourceDetails, Track}
import io.kirill.playlistoptimizer.core.spotify.clients.api.SpotifyResponse._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.FiniteDuration

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

    val audioFeatures = SpotifyAudioFeaturesResponse("1wtxI9YhL1t4yDIwGAFljP", 5, 0, 255348.98, 98.002, 0.613, 0.807)

    "map song and audioFeatures details to a song" in {
      val track = SpotifyMapper.toDomain(song, audioFeatures)

      track must be (Track(
        SongDetails("I'm Not The Only One - Radio Edit", List("Sam Smith", "Bruno Mars"), Some("I'm Not The Only One"), Some(LocalDate.of(2012, 10 ,10)), Some("Single")),
        AudioDetails(98.002, FiniteDuration(255348, TimeUnit.MILLISECONDS), GMinor, 0.807, 0.613),
        SourceDetails("spotify:track:track-id", Some("http://spotify.com/tracks/track-id"))
      ))
    }


    "throw exception when invalid key" in {
      the [InvalidKey] thrownBy {
        SpotifyMapper.toDomain(song, audioFeatures.copy(key = 15))
      }
    }

    "throw exception when invalid mode" in {
      the [InvalidMode] thrownBy {
        SpotifyMapper.toDomain(song, audioFeatures.copy(mode = 2))
      }
    }
  }
}

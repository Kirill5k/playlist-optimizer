package io.kirill.playlistoptimizer.core.spotify.clients.api

import java.time.LocalDate
import java.util.concurrent.TimeUnit
import io.kirill.playlistoptimizer.core.common.errors.{InvalidKey, InvalidMode}
import io.kirill.playlistoptimizer.core.playlist.Key.GMinor
import io.kirill.playlistoptimizer.core.playlist.{AudioDetails, Release, SongDetails, SourceDetails, Track}
import io.kirill.playlistoptimizer.core.spotify.clients.api.SpotifyResponse._
import org.scalatest.Inspectors
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.FiniteDuration

class SpotifyMapperSpec extends AnyWordSpec with Matchers with Inspectors {

  val song = PlaylistTrack(
    "track-id",
    "I'm Not The Only One - Radio Edit",
    PlaylistTrackAlbum(
      "5GWoXPsTQylMuaZ84PC563",
      "Single",
      "I'm Not The Only One",
      Some("2012-10-10"),
      Some("day"),
      List(
        AlbumImage("https://i.scdn.co/image/47421900e7534789603de84c03a40a826c058e45", 640, 640),
        AlbumImage("https://i.scdn.co/image/0d447b6faae870f890dc5780cc58d9afdbc36a1d", 300, 300),
        AlbumImage("https://i.scdn.co/image/d926b3e5f435ef3ac0874b1ff1571cf675b3ef3b", 64, 64)
      )
    ),
    List(PlaylistTrackArtist("2wY79sveU1sp5g7SokKOiI", "Sam Smith"), PlaylistTrackArtist("2wY79sveU1ABCg7SokKOiI", "Bruno Mars")),
    45.0,
    "spotify:track:track-id",
    ExternalUrls("http://spotify.com/tracks/track-id"),
    ExternalIds(Some("ID1"))
  )

  val audioFeatures = SpotifyAudioFeaturesResponse("1wtxI9YhL1t4yDIwGAFljP", 5, 0, 255348.98, 98.002, 0.613, 0.807)

  "A SpotifyMapper" should {

    "map song and audioFeatures details to a song" in {
      val track = SpotifyMapper.toDomain(song, audioFeatures)

      track mustBe Track(
        SongDetails(
          "I'm Not The Only One - Radio Edit",
          List("Sam Smith", "Bruno Mars"),
          Release(
            "I'm Not The Only One",
            "Single",
            Some(LocalDate.of(2012, 10, 10)),
            Some("ID1")
          ),
          Some("https://i.scdn.co/image/47421900e7534789603de84c03a40a826c058e45")
        ),
        AudioDetails(98.002, FiniteDuration(255348, TimeUnit.MILLISECONDS), GMinor, 0.807, 0.613),
        SourceDetails("spotify:track:track-id", Some("http://spotify.com/tracks/track-id"))
      )

    }

    "throw exception when invalid key" in {
      the[InvalidKey] thrownBy {
        SpotifyMapper.toDomain(song, audioFeatures.copy(key = 15))
      }
    }

    "throw exception when invalid mode" in {
      the[InvalidMode] thrownBy {
        SpotifyMapper.toDomain(song, audioFeatures.copy(mode = 2))
      }
    }

    "remove noise words from track search query" in {
      val tracks = Map(
        "bicep - glue (original mix)"   -> "bicep glue",
        "bicep - opal (four tet remix)" -> "bicep opal four tet"
      )

      forAll(tracks) { case (original, expected) =>
        SpotifyMapper.sanitiseTrackSearchQuery(original) mustBe expected
      }
    }
  }
}

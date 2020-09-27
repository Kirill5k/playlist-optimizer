package io.kirill.playlistoptimizer.core

import io.kirill.playlistoptimizer.core.playlist.{AudioDetails, Key, Playlist, PlaylistSource, SongDetails, SourceDetails, Track}

import scala.util.Random
import scala.concurrent.duration._


object BenchmarkUtils {

  def randomizedPlaylist(size: Int)(implicit rand: Random): Playlist = {
    val tracks = (0 until size).map { i =>
      val song = SongDetails(s"song $i", List("test"), None, None, None, None)
      val audio = AudioDetails(124, 5.minutes, Key.values.pickRand, rand.nextDouble(), rand.nextDouble())
      val source = SourceDetails(s"test:$i", None)
      Track(song, audio, source)
    }.toVector
    Playlist(s"randomized-playlist-$size", None, tracks, PlaylistSource.Spotify)
  }

  private implicit class ListOps[A](private val list: List[A]) extends AnyVal {
    def pickRand(implicit rand: Random): A = {
      val i = rand.nextInt(list.size)
      list(i)
    }
  }
}

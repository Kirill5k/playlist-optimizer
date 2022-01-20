package io.kirill.playlistoptimizer.core

import io.kirill.playlistoptimizer.core.playlist.{AudioDetails, Key, Playlist, PlaylistSource, Release, SongDetails, SourceDetails, Track}

import scala.util.Random
import scala.concurrent.duration.*


object BenchmarkUtils {

  def randomizedPlaylist(size: Int)(using rand: Random): Playlist = {
    val tracks = (0 until size).map { i =>
      val song = SongDetails(s"song $i", List("test"), Release("The album", "album", None, None), None)
      val audio = AudioDetails(124, 5.minutes, Key.values.pickRand, rand.nextDouble(), rand.nextDouble())
      val source = SourceDetails(s"test:$i", None)
      Track(song, audio, source)
    }.toVector
    Playlist(s"randomized-playlist-$size", None, tracks, PlaylistSource.Spotify)
  }

  extension [A](list: Array[A])
    def pickRand(using rand: Random): A = {
      val i = rand.nextInt(list.length)
      list(i)
    }
}

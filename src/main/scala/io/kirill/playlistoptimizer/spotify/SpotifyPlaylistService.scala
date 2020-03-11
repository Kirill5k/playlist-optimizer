package io.kirill.playlistoptimizer.spotify

import cats.effect._
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import io.kirill.playlistoptimizer.playlist.{Playlist, PlaylistService}
import sttp.client.{NothingT, SttpBackend}

class SpotifyPlaylistService(accessCode: String)(implicit val C: SpotifyConfig, val B: SttpBackend[IO, Nothing, NothingT]) extends PlaylistService[IO] {


  override def getAll: IO[Seq[Playlist]] = ???
  override def findByName(name: String): IO[Playlist] = ???
  override def optimize(playlist: Playlist): IO[Playlist] = ???
  override def save(playlist: Playlist): IO[Playlist] = ???
}

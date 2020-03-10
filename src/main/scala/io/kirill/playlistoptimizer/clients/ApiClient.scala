package io.kirill.playlistoptimizer.clients

import cats.effect.IO
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import io.kirill.playlistoptimizer.domain.Playlist
import sttp.client.{NothingT, SttpBackend}

trait ApiClient[F[_]] {
  def findPlaylistByName(userId: String, playlistName: String): F[Playlist]
  def savePlaylist(playlist: Playlist): F[Unit]
}

object ApiClient {

  def spotifyClient(implicit C: SpotifyConfig, B: SttpBackend[IO, Nothing, NothingT]): ApiClient[IO] = new SpotifyClient()
}

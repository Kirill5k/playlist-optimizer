package io.kirill.playlistoptimizer.clients

import cats.effect.IO
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import io.kirill.playlistoptimizer.domain.{Playlist}
import sttp.client.{NothingT, SttpBackend}

trait ApiClient[F[_]] {
  def findPlaylistByName(playlistName: String): F[Playlist]
}

object ApiClient {

  def spotifyClient(implicit c: SpotifyConfig, b: SttpBackend[IO, Nothing, NothingT]): ApiClient[IO] = new SpotifyClient()
}

package io.kirill.playlistoptimizer.clients

import cats.effect.IO
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import io.kirill.playlistoptimizer.domain.Playlist
import sttp.client.{NothingT, SttpBackend}

private[clients] class SpotifyClient(implicit val config: SpotifyConfig, val backend: SttpBackend[IO, Nothing, NothingT]) extends ApiClient[IO] {

  override def findPlaylistByName(playlistName: String): IO[Playlist] = ???
}

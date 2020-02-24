package io.kirill.playlistoptimizer.clients

import io.kirill.playlistoptimizer.domain.Playlist

trait ApiClient[F[_]] {
  def getByName(name: String): F[Playlist]
}

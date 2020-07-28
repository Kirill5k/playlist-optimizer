package io.kirill.playlistoptimizer.core.playlist

import cats.MonadError
import cats.effect.Sync
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.playlist.PlaylistOptimizer.OptimizationId
import io.kirill.playlistoptimizer.core.spotify.SpotifyPlaylistService
import sttp.client.{NothingT, SttpBackend}

trait PlaylistService[F[_]] {

  protected def optimizer: PlaylistOptimizer[F]

  def getAll: F[Seq[Playlist]]
  def findByName(name: String): F[Playlist]
  def save(playlist: Playlist): F[Unit]

  def optimize(playlist: Playlist)(implicit m: MonadError[F, Throwable]): F[OptimizationId] =
    optimizer.optimize(playlist)
}

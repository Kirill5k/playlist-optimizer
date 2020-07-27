package io.kirill.playlistoptimizer.core.playlist

import cats.MonadError
import cats.effect.{IO, Sync}
import cats.implicits._
import io.kirill.playlistoptimizer.core.common.config.{AppConfig, SpotifyConfig}
import io.kirill.playlistoptimizer.core.optimizer.Optimizer
import io.kirill.playlistoptimizer.core.spotify.SpotifyPlaylistService
import sttp.client.{NothingT, SttpBackend}

import scala.util.Random

trait PlaylistService[F[_]] {

  protected def optimizer: Optimizer[F, Track]

  def getAll: F[Seq[Playlist]]
  def findByName(name: String): F[Playlist]
  def save(playlist: Playlist): F[Unit]

  def optimize(playlist: Playlist)(implicit m: MonadError[F, Throwable]): F[Playlist] =
    optimizer
      .optimize(playlist.tracks)
      .map(optimizedTracks => playlist.copy(name = s"${playlist.name} optimized", tracks = optimizedTracks))
}

object PlaylistService {
  def spotify[F[_]: Sync](
      optimizer: Optimizer[F, Track],
      backend: SttpBackend[F, Nothing, NothingT],
      spotifyConfig: SpotifyConfig
  ): F[SpotifyPlaylistService[F]] = {
    implicit val sc = spotifyConfig
    implicit val b = backend
    Sync[F].delay(new SpotifyPlaylistService(optimizer))
  }
}

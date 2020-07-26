package io.kirill.playlistoptimizer.playlist

import cats.MonadError
import cats.effect.IO
import cats.implicits._
import io.kirill.playlistoptimizer.common.configs.{AppConfig, SpotifyConfig}
import io.kirill.playlistoptimizer.optimizer.Optimizer
import io.kirill.playlistoptimizer.spotify.SpotifyPlaylistService
import sttp.client.{NothingT, SttpBackend}

import scala.util.Random

trait PlaylistService[F[_]] {
  protected implicit val r: Random

  protected def optimizer: Optimizer[F, Track]

  def getAll: F[Seq[Playlist]]
  def findByName(name: String): F[Playlist]
  def save(playlist: Playlist): F[Unit]

  def optimize(playlist: Playlist)(implicit m: MonadError[F, Throwable]): F[Playlist] =
    optimizer.optimize(playlist.tracks)
      .map(optimizedTracks => playlist.copy(name = s"${playlist.name} optimized", tracks = optimizedTracks))
}

object PlaylistService {
  def spotifyPlaylistService(optimizer: Optimizer[IO, Track])(implicit config: AppConfig, b: SttpBackend[IO, Nothing, NothingT]): SpotifyPlaylistService = {
    implicit val sc: SpotifyConfig = config.spotify
    new SpotifyPlaylistService(optimizer)
  }
}

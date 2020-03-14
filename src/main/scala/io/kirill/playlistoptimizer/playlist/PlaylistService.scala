package io.kirill.playlistoptimizer.playlist

import cats.MonadError
import cats.implicits._
import io.kirill.playlistoptimizer.optimizer.Optimizer

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

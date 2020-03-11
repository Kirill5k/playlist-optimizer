package io.kirill.playlistoptimizer.playlist

import io.kirill.playlistoptimizer.optimizer.Optimizer

import scala.util.Random

trait PlaylistService[F[_]] {
  protected implicit val r: Random

  protected def optimizer: Optimizer[Track]

  def getAll: F[Seq[Playlist]]
  def findByName(name: String): F[Playlist]
  def save(playlist: Playlist): F[Unit]

  def optimize(playlist: Playlist): Playlist = {
    val optimizedTracks = optimizer.optimize(playlist.tracks)
    playlist.copy(name = s"${playlist.name} optimized", tracks = optimizedTracks)
  }
}

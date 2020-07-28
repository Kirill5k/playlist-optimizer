package io.kirill.playlistoptimizer.core.playlist

trait PlaylistService[F[_]] {
  def getAll: F[Seq[Playlist]]
  def findByName(name: String): F[Playlist]
  def save(playlist: Playlist): F[Unit]
}

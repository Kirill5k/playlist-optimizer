package io.kirill.playlistoptimizer.playlist

trait PlaylistService[F[_]] {
  def getAll: F[Seq[Playlist]]
  def findByName(name: String): F[Playlist]
  def optimize(playlist: Playlist): F[Playlist]
  def save(playlist: Playlist): F[Playlist]
}

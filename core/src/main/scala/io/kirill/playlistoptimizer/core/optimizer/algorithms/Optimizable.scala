package io.kirill.playlistoptimizer.core.optimizer.algorithms

import io.kirill.playlistoptimizer.core.playlist.{Playlist, Track}

trait Optimizable[T, I] {
  def repr(target: T): Array[I]
  def update(target: T)(optimizedRepr: Array[I]): T
}

object Optimizable {
  implicit val playlistOptimizable: Optimizable[Playlist, Track] = new Optimizable[Playlist, Track] {
    override def repr(target: Playlist): Array[Track] = target.tracks.toArray[Track]
    override def update(target: Playlist)(optimizedRepr: Array[Track]): Playlist =
      target.copy(
        name = s"${target.name} optimized",
        tracks = optimizedRepr.toVector
      )
  }
}
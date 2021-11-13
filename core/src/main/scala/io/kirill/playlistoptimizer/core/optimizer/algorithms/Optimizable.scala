package io.kirill.playlistoptimizer.core.optimizer.algorithms

import io.kirill.playlistoptimizer.core.playlist.{Playlist, Track}

trait Optimizable[T, I]:
  def repr(target: T): Array[I]
  def update(target: T)(optimizedRepr: Array[I]): T

object Optimizable:
  given playlistOptimizable: Optimizable[Playlist, Track] with
    override inline def repr(target: Playlist): Array[Track] = target.tracks.toArray[Track]
    override inline def update(target: Playlist)(optimizedRepr: Array[Track]): Playlist =
      target.copy(name = s"${target.name} optimized", tracks = optimizedRepr.toVector)

  extension [T, A](target: T)
    def repr(using optimizable: Optimizable[T, A]): Array[A] = optimizable.repr(target)

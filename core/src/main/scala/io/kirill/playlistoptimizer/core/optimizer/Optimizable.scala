package io.kirill.playlistoptimizer.core.optimizer

import io.kirill.playlistoptimizer.core.optimizer.Optimizable
import io.kirill.playlistoptimizer.domain.playlist.{Playlist, Track}

trait Optimizable[T, I]:
  def repr(target: T): Array[I]
  def update(target: T)(optimizedRepr: Array[I]): T

object Optimizable:
  inline given playlistOptimizable: Optimizable[Playlist, Track] with
    override def repr(target: Playlist): Array[Track] = target.tracks.toArray[Track]
    override def update(target: Playlist)(optimizedRepr: Array[Track]): Playlist =
      target.copy(name = s"${target.name} optimized", tracks = optimizedRepr.toVector)

  extension [T, A](target: T)
    def repr(using optimizable: Optimizable[T, A]): Array[A] = optimizable.repr(target)
    def update(optimizedRepr: Array[A])(using optimizable: Optimizable[T, A]): T = optimizable.update(target)(optimizedRepr)

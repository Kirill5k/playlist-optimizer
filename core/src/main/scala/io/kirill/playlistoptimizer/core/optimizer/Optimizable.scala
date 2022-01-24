package io.kirill.playlistoptimizer.core.optimizer

import io.kirill.playlistoptimizer.core.optimizer.Optimizable
import io.kirill.playlistoptimizer.domain.playlist.{Playlist, Track}

trait Optimizable[T, I]:
  extension (target: T)
    def repr: Array[I]
    def update(optimizedRepr: Array[I]): T

object Optimizable:
  inline given playlistOptimizable: Optimizable[Playlist, Track] with
    extension (target: Playlist)
      def repr: Array[Track]                            = target.tracks.toArray[Track]
      def update(optimizedRepr: Array[Track]): Playlist = target.copy(name = s"${target.name} optimized", tracks = optimizedRepr.toVector)

package io.kirill.playlistoptimizer.domain.utils

object common:
  extension (bool: Boolean)
    def toInt = if (bool) 1 else 0

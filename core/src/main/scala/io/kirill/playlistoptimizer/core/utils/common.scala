package io.kirill.playlistoptimizer.core.utils

object common:
  
  extension (bool: Boolean)
    def toInt = if (bool) 1 else 0

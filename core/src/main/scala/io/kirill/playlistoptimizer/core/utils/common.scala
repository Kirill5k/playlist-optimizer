package io.kirill.playlistoptimizer.core.utils

object common {
  implicit class BooleanModifier(private val bool: Boolean) extends AnyVal {
    def toInt = if (bool) 1 else 0
  }
}










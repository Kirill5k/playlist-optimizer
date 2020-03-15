package io.kirill.playlistoptimizer.utils

object CommonUtils {
  implicit class BooleanModifier(private val bool: Boolean) extends AnyVal {
    def toInt = if (bool) 1 else 0
  }
}

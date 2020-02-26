package io.kirill.playlistoptimizer.utils

object CommonUtils {
  implicit class BooleanModifier(val bool: Boolean) extends AnyVal {
    def toInt = if(bool) 1 else 0
  }
}

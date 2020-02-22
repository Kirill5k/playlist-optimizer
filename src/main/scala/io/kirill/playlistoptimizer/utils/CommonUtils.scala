package io.kirill.playlistoptimizer.utils

object CommonUtils {
  implicit class BooleanModifier(bool: Boolean) {
    def toInt = if(bool) 1 else 0
  }
}

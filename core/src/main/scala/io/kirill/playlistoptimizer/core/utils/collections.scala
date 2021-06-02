package io.kirill.playlistoptimizer.core.utils

object collections {

  implicit class SeqOps[A](private val seq: Seq[A]) extends AnyVal {
    def pairs: Seq[(A, A)] =
      seq.zip(seq.tail).zipWithIndex collect { case (x, i) if (i + 1) % 2 != 0 => x }
  }
}

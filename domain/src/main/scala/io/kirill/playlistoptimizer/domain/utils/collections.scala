package io.kirill.playlistoptimizer.domain.utils

object collections:
  extension [A](seq: Seq[A])
    def pairs: Seq[(A, A)] =
      seq.zip(seq.tail).zipWithIndex collect { case (x, i) if (i + 1) % 2 != 0 => x }

package io.kirill.playlistoptimizer.core.utils

import scala.util.Random

object CollectionOps {

  implicit class SeqOps[A](private val seq: Seq[A]) extends AnyVal {

    def splitInThree(p1: Int, p2: Int): (Seq[A], Seq[A], Seq[A]) = {
      if (p1 >= p2) throw new IllegalArgumentException("point 1 must be less than point 2")
      else if (p1 > seq.size || p2 > seq.size) throw new IllegalArgumentException("points cannot be greater than size")
      else {
        val (l, rest) = seq.splitAt(p1)
        val (m, r) = rest.splitAt(p2-p1-1)
        (l, m, r)
      }
    }

    def swap(pos1: Int, pos2: Int): Seq[A] =
      if (pos1 >= seq.size || pos2 >= seq.size) throw new IllegalArgumentException("pos cannot be greater than size")
      else if (pos1 == pos2) seq
      else seq.updated(pos1, seq(pos2)).updated(pos2, seq(pos1))

    def pairs: Seq[(A, A)] =
      seq.zip(seq.tail).zipWithIndex collect { case (x, i) if (i + 1) % 2 != 0 => x }

    def shuffledCopies(n: Int): Seq[Seq[A]] = List.fill(n)(Random.shuffle(seq))
  }
}

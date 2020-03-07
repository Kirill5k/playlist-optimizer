package io.kirill.playlistoptimizer.utils

object CollectionOps {
  implicit class IndexedSeqOps[A](val vector: IndexedSeq[A]) extends AnyVal {
    def swap(pos1: Int, pos2: Int): IndexedSeq[A] =
      if (pos1 >= vector.size || pos2 >= vector.size) throw new IllegalArgumentException("pos cannot be greater than size")
      else if (pos1 == pos2) vector
      else vector.updated(pos1, vector(pos2)).updated(pos2, vector(pos1))
  }

  implicit class SeqOps[A](seq: Seq[A]) {
    def removeNth(n: Int): Seq[A] = seq.zipWithIndex collect { case (x,i) if (i + 1) % n != 0 => x }

    def splitInThree(p1: Int, p2: Int): (Seq[A], Seq[A], Seq[A]) = {
      if (p1 >= p2) throw new IllegalArgumentException("point 1 must be less than point 2")
      else if (p1 > seq.size || p2 > seq.size) throw new IllegalArgumentException("points cannot be greater than size")
      else {
        val (l, rest) = seq.splitAt(p1)
        val (m, r) = rest.splitAt(p2-p1-1)
        (l, m, r)
      }
    }
  }
}

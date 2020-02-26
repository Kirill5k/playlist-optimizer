package io.kirill.playlistoptimizer.utils

object CollectionOps {
  implicit class IndexedSeqOps[A](val vector: IndexedSeq[A]) extends AnyVal {
    def swap(pos1: Int, pos2: Int): IndexedSeq[A] =
      if (pos1 >= vector.size || pos2 >= vector.size) throw new IllegalArgumentException("pos cannot be greater than size")
      else if (pos1 == pos2) vector
      else vector.updated(pos1, vector(pos2)).updated(pos2, vector(pos1))

    def splitInThree(point1: Int, point2: Int): (IndexedSeq[A], IndexedSeq[A], IndexedSeq[A]) = {
      if (point1 >= point2) throw new IllegalArgumentException("point 1 must be less than point 2")
      else if (point1 > vector.size || point2 > vector.size) throw new IllegalArgumentException("points cannot be greater than size")
      else {
        val (l, rest) = vector.splitAt(point1)
        val (m, r) = rest.splitAt(point2-point1-1)
        (l, m, r)
      }
    }
  }

  implicit class ListOps[A](list: List[A]) {
    def removeNth(n: Int): List[A] = list.zipWithIndex collect { case (x,i) if (i + 1) % n != 0 => x }
  }
}

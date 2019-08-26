package playlistoptimizer

object Utils {
  implicit class VectorModifier[A](vector: Vector[A]) {
    def swap(pos1: Int, pos2: Int): Vector[A] =
      if (pos1 >= vector.size || pos2 >= vector.size) throw new IllegalArgumentException("pos cannot be greater than size")
      else if (pos1 == pos2) vector
      else vector.updated(pos1, vector(pos2)).updated(pos2, vector(pos1))

    def splitInThree(point1: Int, point2: Int): (Vector[A], Vector[A], Vector[A]) = {
      if (point1 >= point2) throw new IllegalArgumentException("point 1 must be less than point 2")
      else if (point1 >= vector.size || point2 >= vector.size) throw new IllegalArgumentException("points cannot be greater than size")
      else {
        val (l, m1) = vector.splitAt(point1)
        val (m2, r) = vector.splitAt(point2)
        (l, m1++m2, r)
      }
    }
  }

  implicit class ListModifier[A](list: List[A]) {
    def removeNth(n: Int): List[A] = list.zipWithIndex collect { case (x,i) if (i + 1) % n != 0 => x }
  }

  implicit class BooleanModifier(bool: Boolean) {
    def toInt = if(bool) 1 else 0
  }
}

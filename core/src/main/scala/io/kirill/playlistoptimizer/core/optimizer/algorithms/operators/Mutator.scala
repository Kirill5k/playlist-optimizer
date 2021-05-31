package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import scala.reflect.ClassTag
import scala.util.Random

trait Mutator[A] {
  def mutate(ind: IndexedSeq[A], mutationProbability: Double)(implicit r: Random): IndexedSeq[A]
}

object Mutator {
  def randomSwapMutator[A: ClassTag]: Mutator[A] = new Mutator[A] {
    override def mutate(ind: IndexedSeq[A], mutationFactor: Double)(implicit r: Random): IndexedSeq[A] = {
      val swaps = math.round(ind.size * mutationFactor / 2.0).toInt
      val result = ind.toArray[A]
      var i = 0
      while (i < swaps) {
        val p1 = r.nextInt(ind.size)
        val p2 = r.nextInt(ind.size)
        val ind1 = result(p1)
        result(p1) = result(p2)
        result(p2) = ind1
        i += 1
      }

      result.toVector
    }
  }

  def neighbourSwapMutator[A: ClassTag]: Mutator[A] = new Mutator[A] {
    override def mutate(ind: IndexedSeq[A], mutationFactor: Double)(implicit r: Random): IndexedSeq[A] = {
      val result = ind.toArray[A]
      var i = 0
      while (i < result.length - 1) {
        if (r.nextDouble() < mutationFactor) {
          val curr = result(i)
          val next = result(i+1)
          result(i) = next
          result(i+1) = curr
        }
        i += 1
      }

      result.toVector
    }
  }

}

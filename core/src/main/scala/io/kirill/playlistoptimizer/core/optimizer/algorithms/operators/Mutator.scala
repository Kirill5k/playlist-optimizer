package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.utils.CollectionOps._

import scala.util.Random

sealed trait Mutator[A] {
  def mutate(ind: IndexedSeq[A])(implicit r: Random): IndexedSeq[A]

  def mutate(ind: IndexedSeq[A], mutationProbability: Double)(implicit r: Random): IndexedSeq[A] = {
    val n = r.nextDouble()
    if (n < mutationProbability) mutate(ind) else ind
  }
}

object Mutator {
  implicit def randomSwapMutator[A]: Mutator[A] = new Mutator[A] {
    override def mutate(ind: IndexedSeq[A])(implicit r: Random): IndexedSeq[A] =
      ind.swap(r.nextInt(ind.size), r.nextInt(ind.size))
  }

  implicit def neighbourSwapMutator[A](implicit r: Random): Mutator[A] = new Mutator[A] {
    override def mutate(ind: IndexedSeq[A])(implicit r: Random): IndexedSeq[A] = {
      val p1 = r.nextInt(ind.size)
      val p2 = if (p1 > ind.size/2) p1 - 1 else p1 + 1
      ind.swap(p1, p2)
    }
  }

}

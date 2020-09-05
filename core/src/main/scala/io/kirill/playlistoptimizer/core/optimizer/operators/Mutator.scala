package io.kirill.playlistoptimizer.core.optimizer.operators

import io.kirill.playlistoptimizer.core.utils.CollectionOps._

import scala.util.Random

sealed trait Mutator[A] {
  def mutate(s: IndexedSeq[A])(implicit r: Random): IndexedSeq[A]
}

object Mutator {
  implicit def randomSwapMutator[A]: Mutator[A] = new Mutator[A] {
    override def mutate(s: IndexedSeq[A])(implicit r: Random): IndexedSeq[A] =
      s.swap(r.nextInt(s.size), r.nextInt(s.size))
  }

  implicit def neighbourSwapMutator[A](implicit r: Random): Mutator[A] = new Mutator[A] {
    override def mutate(s: IndexedSeq[A])(implicit r: Random): IndexedSeq[A] = {
      val p1 = r.nextInt(s.size)
      val p2 = if (p1 > s.size/2) p1 - 1 else p1 + 1
      s.swap(p1, p2)
    }
  }

}

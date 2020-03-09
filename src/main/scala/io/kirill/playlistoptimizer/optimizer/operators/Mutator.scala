package io.kirill.playlistoptimizer.optimizer.operators

import io.kirill.playlistoptimizer.utils.CollectionOps._

import scala.util.Random

sealed trait Mutator[A] {
  def mutate(s: Seq[A]): Seq[A]
}

object Mutator {
  implicit def randomSwapMutator[A](implicit R: Random): Mutator[A] = new Mutator[A] {
    override def mutate(s: Seq[A]): Seq[A] =
      s.swap(R.nextInt(s.size), R.nextInt(s.size))
  }

  implicit def neighbourSwapMutator[A](implicit R: Random): Mutator[A] = new Mutator[A] {
    override def mutate(s: Seq[A]): Seq[A] = {
      val p1 = R.nextInt(s.size)
      val p2 = if (p1 > s.size/2) p1 - 1 else p1 + 1
      s.swap(p1, p2)
    }
  }

}

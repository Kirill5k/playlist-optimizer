package io.kirill.playlistoptimizer.optimizer.operators

import io.kirill.playlistoptimizer.utils.CollectionOps._

import scala.util.Random

sealed trait Mutator[A] {
  def mutate(s: Seq[A]): Seq[A]
}

object Mutator {
  implicit def randomSwapMutator[A](implicit R: Random): Mutator[A] = new Mutator[A] {
    override def mutate(s: Seq[A]): Seq[A] = {
      s.swap(R.nextInt(s.size), R.nextInt(s.size))
    }
  }
}

package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.utils.CollectionOps._

import scala.util.Random

sealed trait Mutator[A] {
  def mutate(ind: IndexedSeq[A], mutationProbability: Double)(implicit r: Random): IndexedSeq[A]
}

object Mutator {
  implicit def randomSwapMutator[A]: Mutator[A] = new Mutator[A] {

    override def mutate(ind: IndexedSeq[A], mutationFactor: Double)(implicit r: Random): IndexedSeq[A] = {
      val n = r.nextDouble()
      if (n < mutationFactor) ind.swap(r.nextInt(ind.size), r.nextInt(ind.size)) else ind
    }
  }

  implicit def neighbourSwapMutator[A]: Mutator[A] = new Mutator[A] {

    override def mutate(ind: IndexedSeq[A], mutationFactor: Double)(implicit r: Random): IndexedSeq[A] = {
      ind.toList.tail.foldLeft[List[A]](List(ind.head)) {
        case (last :: tail, el) =>
          val n = r.nextDouble()
          if (n < mutationFactor) last :: el :: tail
          else el :: last :: tail
      }.reverse.toVector
    }
  }

}

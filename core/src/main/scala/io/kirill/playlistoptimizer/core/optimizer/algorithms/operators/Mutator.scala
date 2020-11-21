package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.utils.CollectionOps._

import scala.util.Random

sealed trait Mutator[A] {
  def mutate(ind: IndexedSeq[A], mutationProbability: Double)(implicit r: Random): IndexedSeq[A]
}

object Mutator {
  implicit def randomSwapMutator[A]: Mutator[A] = new Mutator[A] {
    override def mutate(ind: IndexedSeq[A], mutationFactor: Double)(implicit r: Random): IndexedSeq[A] = {
      val swaps = math.round(ind.size * mutationFactor / 2.0)
      (0 until swaps.toInt).foldLeft(ind)((res, _) => res.swap(r.nextInt(ind.size), r.nextInt(ind.size)))
    }
  }

  implicit def neighbourSwapMutator[A]: Mutator[A] = new Mutator[A] {
    override def mutate(ind: IndexedSeq[A], mutationFactor: Double)(implicit r: Random): IndexedSeq[A] = {
      ind.toList.foldLeft[List[A]](Nil) {
        case (Nil, el) =>
          List(el)
        case (last :: tail, el) =>
          val n = r.nextDouble()
          if (n < mutationFactor) last :: el :: tail
          else el :: last :: tail
      }.reverse.toVector
    }
  }

}

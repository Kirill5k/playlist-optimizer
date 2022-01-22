package io.kirill.playlistoptimizer.free.operators

import scala.util.Random

trait Mutator[A]:
  def mutate(ind: Array[A], mutationProbability: Double)(using r: Random): Array[A]

object Mutator:
  inline def randomSwapMutator[A]: Mutator[A] = new Mutator[A] {
    override def mutate(ind: Array[A], mutationFactor: Double)(using r: Random): Array[A] = {
      val swaps = ind.length * mutationFactor / 2.0
      val result = ind.clone()
      var i = 0
      while (i < swaps) {
        val p1 = r.nextInt(ind.length)
        val p2 = r.nextInt(ind.length)
        val ind1 = result(p1)
        result(p1) = result(p2)
        result(p2) = ind1
        i += 1
      }

      result
    }
  }

  inline def neighbourSwapMutator[A]: Mutator[A] = new Mutator[A] {
    override def mutate(ind: Array[A], mutationFactor: Double)(using r: Random): Array[A] = {
      val result = ind.clone()
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

      result
    }
  }

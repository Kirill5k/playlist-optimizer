package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

trait Elitism[A] {
  def select(population: Seq[(IndexedSeq[A], Double)], elitismRatio: Double): Seq[IndexedSeq[A]]
}

object Elitism {
  def elitism[A]: Elitism[A] = new Elitism[A] {
    override def select(population: Seq[(IndexedSeq[A], Double)], elitismRatio: Double): Seq[IndexedSeq[A]] = {
      val n = population.size * elitismRatio
      population.sortBy(_._2).map(_._1).take(n.toInt)
    }
  }
}

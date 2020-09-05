package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

trait Elitism[A] {
  def select(population: Seq[(A, Double)], elitismRatio: Double): Seq[A]
}

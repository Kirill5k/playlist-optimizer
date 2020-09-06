package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators.operators.Fitness

trait Elitism[A] {
  def select(population: Seq[(IndexedSeq[A], Fitness)], elitismRatio: Double): Seq[IndexedSeq[A]]
}

object Elitism {
  def elitism[A]: Elitism[A] = new Elitism[A] {
    override def select(population: Seq[(IndexedSeq[A], Fitness)], elitismRatio: Double): Seq[IndexedSeq[A]] = {
      val n = population.size * elitismRatio
      population.sortBy(_._2.value).map(_._1).take(n.toInt)
    }
  }
}
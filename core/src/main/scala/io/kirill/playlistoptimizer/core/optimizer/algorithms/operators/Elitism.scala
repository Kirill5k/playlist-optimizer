package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

trait Elitism[A]:
  def select(population: Seq[(Array[A], Fitness)], elitismRatio: Double): Seq[Array[A]]

object Elitism:
  inline def simple[A]: Elitism[A] = new Elitism[A] {
    override def select(population: Seq[(Array[A], Fitness)], elitismRatio: Double): Seq[Array[A]] = {
      val n = population.size * elitismRatio
      population.sortBy(_._2).take(n.toInt).map(_._1)
    }
  }

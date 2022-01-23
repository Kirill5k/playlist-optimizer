package io.kirill.playlistoptimizer.free.operators

import io.kirill.playlistoptimizer.free.Fitness
import io.kirill.playlistoptimizer.free.{EvaluatedPopulation, Population}

trait Elitism[A]:
  def select(population: EvaluatedPopulation[A], n: Double): Population[A]

object Elitism:
  inline def simple[A]: Elitism[A] = new Elitism[A] {
    override def select(population: EvaluatedPopulation[A], n: Double): Population[A] =
      population.sortBy(_._2).take(n.toInt).map(_._1)
  }

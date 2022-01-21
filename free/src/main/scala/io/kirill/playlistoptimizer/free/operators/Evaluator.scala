package io.kirill.playlistoptimizer.free.operators

import io.kirill.playlistoptimizer.free.Fitness

trait Evaluator[A]:
  def evaluateIndividual(individual: Array[A]): Fitness

  def evaluatePopulation(population: Seq[Array[A]]): Seq[(Array[A], Fitness)] =
    population.map(ind => (ind, evaluateIndividual(ind)))

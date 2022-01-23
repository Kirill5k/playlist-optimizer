package io.kirill.playlistoptimizer.free.operators

import io.kirill.playlistoptimizer.free.{Fitness, Ind}

trait Evaluator[A]:
  def evaluateIndividual(individual: Ind[A]): (Ind[A], Fitness)

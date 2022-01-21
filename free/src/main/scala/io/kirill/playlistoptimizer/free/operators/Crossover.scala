package io.kirill.playlistoptimizer.free.operators

import scala.util.Random

trait Crossover[A]:
  def cross(par1: Array[A], par2: Array[A])(using r: Random): Array[A]

  def cross(par1: Array[A], par2: Array[A], crossoverProbability: Double)(using r: Random): Array[A] =
    if r.nextDouble() < crossoverProbability then cross(par1, par2) else par1

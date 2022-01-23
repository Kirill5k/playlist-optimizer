package io.kirill.playlistoptimizer.free.operators

import io.kirill.playlistoptimizer.free.Ind
import scala.util.Random

trait Crossover[A]:
  def cross(par1: Ind[A], par2: Ind[A])(using r: Random): Ind[A]

  def cross(par1: Ind[A], par2: Ind[A], crossoverProbability: Double)(using r: Random): Ind[A] =
    if r.nextDouble() < crossoverProbability then cross(par1, par2) else par1

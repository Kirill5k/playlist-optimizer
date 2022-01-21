package io.kirill.playlistoptimizer.free

opaque type Fitness = BigDecimal
object Fitness:
  def apply(value: BigDecimal): Fitness              = value
  extension (fitness: Fitness) def value: BigDecimal = fitness
  given ordering: Ordering[Fitness] with
    def compare(f1: Fitness, f2: Fitness) = f1.compare(f2)

type Ind                   = [G] =>> Array[G]
type Population            = [G] =>> List[Ind[G]]
type EvaluatedPopulation   = [G] =>> List[(Ind[G], Fitness)]
type DistributedPopulation = [G] =>> List[(Ind[G], Ind[G])]

enum Op[A]:
  case InitPopulation[G](seed: Ind[G], size: Int, shuffle: Boolean) extends Op[Population[G]]
  case Crossover[G](ind1: Ind[G], ind2: Ind[G], prob: Double) extends Op[Ind[G]]
  case Mutate[G](ind: Ind[G], prob: Double) extends Op[Ind[G]]
  case EvaluateOne[G](ind: Ind[G]) extends Op[(Ind[G], Fitness)]
  case EvaluatePopulation[G](population: Population[G]) extends Op[EvaluatedPopulation[G]]
  case SelectElites[G](population: EvaluatedPopulation[G], popSize: Int, ratio: Double) extends Op[Population[G]]
  case SelectPairs[G](population: EvaluatedPopulation[G], limit: Int) extends Op[DistributedPopulation[G]]
  case SelectFittest[G](population: EvaluatedPopulation[G]) extends Op[Ind[G]]
  case ApplyToAll[A, B](population: List[A], op: A => Op[B]) extends Op[List[B]]

object Op {}

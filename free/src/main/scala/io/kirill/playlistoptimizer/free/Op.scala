package io.kirill.playlistoptimizer.free

opaque type Fitness = BigDecimal

type Ind                   = [G] =>> Array[G]
type Population            = [G] =>> List[Ind[G]]
type EvaluatedPopulation   = [G] =>> List[(Ind[G], Fitness)]
type DistributedPopulation = [G] =>> List[(Ind[G], Ind[G])]

enum Op[A]:
  case InitPopulation[G](seed: Ind[G]) extends Op[Population[G]]
  case Crossover[G](ind1: Ind[G], ind2: Ind[G]) extends Op[Ind[G]]
  case Mutate[G](ind: Ind[G]) extends Op[Ind[G]]
  case EvaluateOne[G](ind: Ind[G]) extends Op[(Ind[G], Fitness)]
  case EvaluatePopulation[G](population: Population[G]) extends Op[EvaluatedPopulation[G]]
  case SelectElites[G](population: EvaluatedPopulation[G]) extends Op[Population[G]]
  case SelectPairs[G](population: EvaluatedPopulation[G]) extends Op[DistributedPopulation[G]]
  case SelectFittest[G](population: EvaluatedPopulation[G]) extends Op[Ind[G]]

  case Pure[A](value: A) extends Op[A]
  case Bind[A, B](op: Op[A], f: A => Op[B]) extends Op[B]
  case Traverse[A, B](ops: List[A], f: A => Op[B]) extends Op[List[B]]
  case Iterate[A](a: A, f: A => Op[A]) extends Op[A]

  def flatMap[B](f: A => Op[B]): Op[B] = Bind(this, f)
  def map[B](f: A => B): Op[B]         = Bind(this, a => Pure(f(a)))

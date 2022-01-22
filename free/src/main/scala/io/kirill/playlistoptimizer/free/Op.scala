package io.kirill.playlistoptimizer.free

import cats.effect.Sync
import cats.~>
import io.kirill.playlistoptimizer.free.operators.*

import scala.reflect.ClassTag
import scala.util.Random

opaque type Fitness = BigDecimal
object Fitness:
  def apply(value: BigDecimal): Fitness              = value
  extension (fitness: Fitness) def value: BigDecimal = fitness
  given ordering: Ordering[Fitness] with
    def compare(f1: Fitness, f2: Fitness) = f1.compare(f2)

type Ind[G]                   = Array[G]
type Population[G]            = List[Ind[G]]
type EvaluatedPopulation[G]   = List[(Ind[G], Fitness)]
type DistributedPopulation[G] = List[(Ind[G], Ind[G])]

enum Op[A, G]:
  case InitPopulation[G](seed: Ind[G], size: Int, shuffle: Boolean) extends Op[Population[G], G]
  case Cross[G](ind1: Ind[G], ind2: Ind[G], prob: Double) extends Op[Ind[G], G]
  case Mutate[G](ind: Ind[G], prob: Double) extends Op[Ind[G], G]
  case EvaluateOne[G](ind: Ind[G]) extends Op[(Ind[G], Fitness), G]
  case EvaluatePopulation[G](population: Population[G]) extends Op[EvaluatedPopulation[G], G]
  case SelectElites[G](population: EvaluatedPopulation[G], popSize: Int, ratio: Double) extends Op[Population[G], G]
  case SelectPairs[G](population: EvaluatedPopulation[G], limit: Int) extends Op[DistributedPopulation[G], G]
  case SelectFittest[G](population: EvaluatedPopulation[G]) extends Op[Ind[G], G]
  case ApplyToAll[A, B, G](population: List[A], op: A => Op[B, G]) extends Op[List[B], G]

object Op {
  def ioInterpreter[F[_], G](
      crossover: Crossover[G],
      mutator: Mutator[G],
      evaluator: Evaluator[G],
      selector: Selector[G],
      elitism: Elitism[G]
  )(using F: Sync[F], rand: Random): Op[*, G] ~> F = new (Op[*, G] ~> F) {
    def apply[A](fa: Op[A, G]): F[A] =
      fa match {
        case Op.InitPopulation(seed, size, shuffle)      => ???
        case Op.Cross(ind1, ind2, prob)                  => ???
        case Op.Mutate(ind, prob)                        => F.delay(mutator.mutate(ind, prob))
        case Op.EvaluateOne(ind)                         => ???
        case Op.EvaluatePopulation(population)           => ???
        case Op.SelectElites(population, popSize, ratio) => ???
        case Op.SelectPairs(population, limit)           => ???
        case Op.SelectFittest(population)                => ???
        case Op.ApplyToAll(population, op)               => ???
        case _ | null                                    => ???
      }
  }
}

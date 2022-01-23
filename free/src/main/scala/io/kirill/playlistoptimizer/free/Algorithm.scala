package io.kirill.playlistoptimizer.free

import cats.free.Free

trait Algorithm:
  def optimize[G](target: Ind[G], params: Algorithm.OptimizationParameters): Free[Op[*, G], (Ind[G], Fitness)]

object Algorithm:

  final case class OptimizationParameters(
      populationSize: Int,
      maxGen: Int,
      crossoverProbability: Double,
      mutationProbability: Double,
      elitismRatio: Double,
      shuffle: Boolean
  )

  case object GeneticAlgorithm extends Algorithm {
    override def optimize[G](target: Ind[G], params: OptimizationParameters): Free[Op[*, G], (Ind[G], Fitness)] =
      for
        pop      <- Op.InitPopulation(target, params.populationSize, params.shuffle).freeM
        finalPop <- iterate(pop, params.maxGen) { currentPop =>
          for
            evPop    <- Op.EvaluatePopulation(currentPop).freeM
            elites   <- Op.SelectElites(evPop, params.populationSize, params.elitismRatio).freeM
            pairs    <- Op.SelectPairs(evPop, params.populationSize).freeM
            crossed1 <- Op.ApplyToAll(pairs, (i1, i2) => Op.Cross(i1, i2, params.crossoverProbability)).freeM
            crossed2 <- Op.ApplyToAll(pairs, (i1, i2) => Op.Cross(i2, i1, params.crossoverProbability)).freeM
            mutated  <- Op.ApplyToAll(crossed1 ++ crossed2, i => Op.Mutate(i, params.mutationProbability)).freeM
          yield mutated ++ elites
        }
        evPop    <- Op.EvaluatePopulation(finalPop).freeM
        fittest  <- Op.SelectFittest(evPop).freeM
      yield fittest
  }

  private def iterate[F[_], A](a: A, n: Int)(f: A => Free[F, A]): Free[F, A] =
    LazyList.fill(n)(0).foldLeft[Free[F, A]](Free.pure(a))((res, _) => res.flatMap(f))

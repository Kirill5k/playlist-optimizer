package io.kirill.playlistoptimizer.free

trait Algorithm:
  def optimize[G](target: Ind[G], params: Algorithm.OptimizationParameters): Op[Ind[G]]

object Algorithm:
  final case class OptimizationParameters(
      populationSize: Int,
      maxGen: Int,
      crossoverProbability: Double,
      mutationProbability: Double,
      elitismRatio: Double,
      shuffle: Boolean
  )

  val genAlg = new Algorithm {
    override def optimize[G](target: Ind[G], params: OptimizationParameters): Op[Ind[G]] = {
      val singleIteration: (pop: Population[G]) => Op[Population[G]] = pop =>
        for
          evPop    <- Op.EvaluatePopulation(pop)
          elites   <- Op.SelectElites(evPop, params.populationSize, params.elitismRatio)
          pairs    <- Op.SelectPairs(evPop, params.populationSize)
          crossed1 <- Op.Traverse(pairs, (i1, i2) => Op.Crossover(i1, i2, params.crossoverProbability))
          crossed2 <- Op.Traverse(pairs, (i1, i2) => Op.Crossover(i2, i1, params.crossoverProbability))
          mutated  <- Op.Traverse(crossed1 ++ crossed2, i => Op.Mutate(i, params.mutationProbability))
        yield mutated ++ elites

      for
        pop      <- Op.InitPopulation(target, params.populationSize, params.shuffle)
        finalPop <- Op.Iterate(pop, params.maxGen, singleIteration)
        evPop    <- Op.EvaluatePopulation(finalPop)
        fittest  <- Op.SelectFittest(evPop)
      yield fittest
    }
  }

package io.kirill.playlistoptimizer.free

trait Algorithm:
  def optimize[G](target: Ind[G]): Op[Ind[G]]

object Algorithm:
  val genAlg = new Algorithm {
    override def optimize[G](target: Ind[G]): Op[Ind[G]] = {
      val singleIteration: (pop: Population[G]) => Op[Population[G]] = pop =>
        for
          evPop    <- Op.EvaluatePopulation(pop)
          elites   <- Op.SelectElites(evPop)
          pairs    <- Op.SelectPairs(evPop)
          crossed1 <- Op.Traverse(pairs, (i1, i2) => Op.Crossover(i1, i2))
          crossed2 <- Op.Traverse(pairs, (i1, i2) => Op.Crossover(i2, i1))
          mutated  <- Op.Traverse(crossed1 ++ crossed2, i => Op.Mutate(i))
        yield mutated ++ elites

      for
        pop      <- Op.InitPopulation(target)
        finalPop <- Op.Iterate(pop, singleIteration)
        evPop    <- Op.EvaluatePopulation(finalPop)
        fittest  <- Op.SelectFittest(evPop)
      yield fittest
    }
  }

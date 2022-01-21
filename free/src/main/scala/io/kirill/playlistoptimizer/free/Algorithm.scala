package io.kirill.playlistoptimizer.free

trait Algorithm:
  def optimize[G](target: Ind[G], params: Algorithm.OptimizationParameters): Free[Op, Ind[G]]

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
    override def optimize[G](target: Ind[G], params: OptimizationParameters): Free[Op, Ind[G]] = {
      val singleIteration: Population[G] => Free[Op, Population[G]] = pop =>
        for
          evPop    <- Op.EvaluatePopulation(pop).freeM
          elites   <- Op.SelectElites(evPop, params.populationSize, params.elitismRatio).freeM
          pairs    <- Op.SelectPairs(evPop, params.populationSize).freeM
          crossed1 <- pairs.traverse((i1, i2) => Op.Crossover(i1, i2, params.crossoverProbability))
          crossed2 <- pairs.traverse((i1, i2) => Op.Crossover(i2, i1, params.crossoverProbability))
          mutated  <- (crossed1 ++ crossed2).traverse(i => Op.Mutate(i, params.mutationProbability))
        yield mutated ++ elites

      for
        pop      <- Op.InitPopulation(target, params.populationSize, params.shuffle).freeM
        finalPop <- Free.iterate(pop, params.maxGen)(singleIteration)
        evPop    <- Op.EvaluatePopulation(finalPop).freeM
        fittest  <- Op.SelectFittest(evPop).freeM
      yield fittest
    }
  }

  extension [A](fa: Op[A])
    def freeM: Free[Op, A] = Free.liftM(fa)

  extension [A](ls: List[A])
    def traverse[B](f: A => Op[B]): Free[Op, List[B]] = Free.traverse(ls, f)

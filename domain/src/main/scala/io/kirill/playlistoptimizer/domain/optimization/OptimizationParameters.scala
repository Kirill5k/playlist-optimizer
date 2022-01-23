package io.kirill.playlistoptimizer.domain.optimization

final case class OptimizationParameters(
    populationSize: Int,
    maxGen: Int,
    crossoverProbability: Double,
    mutationProbability: Double,
    elitismRatio: Double,
    shuffle: Boolean
)

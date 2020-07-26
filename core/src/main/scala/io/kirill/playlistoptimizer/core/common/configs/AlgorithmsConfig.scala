package io.kirill.playlistoptimizer.core.common.configs

final case class GeneticAlgorithmConfig(populationSize: Int, iterations: Int, mutationFactor: Double)

final case class AlgorithmsConfig(ga: GeneticAlgorithmConfig)

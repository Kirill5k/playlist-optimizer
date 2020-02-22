package io.kirill.playlistoptimizer.optimizer

import io.kirill.playlistoptimizer.domain.Playlist

import scala.util.Random

object OptimizerInstances {
  implicit val geneticAlgorithmOptimizer = new Optimizer {
    override def optimize(playlist: Playlist)(implicit random: Random): Playlist = GeneticAlgorithmOptimizer(100, 100, 0.2).optimize(playlist)
  }
}
package io.kirill.playlistoptimizer.optimizer

import io.kirill.playlistoptimizer.domain.{Key, Playlist}
import io.kirill.playlistoptimizer.utils.CollectionOps._
import io.kirill.playlistoptimizer.domain.{Key, Playlist}

import scala.util.Random

trait Optimizer {
  def optimize(playlist: Playlist)(implicit random: Random): Playlist
}

object Optimizer {
  private[optimizer] def distributeInPairs[A](population: List[A]): List[(A, A)] = {
    val half = population.removeNth(2)
    half.zip(population.filterNot(half.contains))
  }

  private[optimizer] def mutate(pl: Playlist)(implicit rnd: Random): Playlist =
    pl.map(_.swap(rnd.nextInt(pl.size), rnd.nextInt(pl.size)))

  private[optimizer] def crossover(pl1: Playlist, pl2: Playlist)(implicit rnd: Random): Playlist = pl1.zipWith(pl2)((s1, s2) => {
    val half = s1.size / 2
    val point1: Int = rnd.nextInt(half)
    val point2: Int = rnd.nextInt(half) + half
    val (l, m, r) = s1.splitInThree(point1, point2)
    l ++ s2.filter(m.contains) ++ r
  })

  private[optimizer] def evaluate(pl: Playlist): Int = pl.reduce(_.sliding(2).foldLeft(0) { case (acc, s1 +: s2 +: _) => Key.distance(s1.key, s2.key) + acc })
}

case class GeneticAlgorithmOptimizer(populationSize: Int, iterations: Int, mutationFactor: Double) extends Optimizer {
  import Optimizer._

  override def optimize(initialPlaylist: Playlist)(implicit rnd: Random): Playlist = {
    val initialPopulation: List[Playlist] = List.fill(populationSize)(initialPlaylist.map(Random.shuffle(_)))
    (0 until populationSize)
      .foldLeft(initialPopulation)((currentPopulation, _) => singleIteration(currentPopulation))
      .head
  }

  private def singleIteration(population: List[Playlist])(implicit rnd: Random): List[Playlist] = {
    val newPopulation = distributeInPairs(population)
      .flatMap { case (p1, p2) => List(crossover(p1, p2), crossover(p2, p1)) }
      .map(m => if (rnd.nextDouble < mutationFactor) Optimizer.mutate(m) else m)

    (newPopulation ++ population).sortBy(evaluate).take(populationSize)
  }
}

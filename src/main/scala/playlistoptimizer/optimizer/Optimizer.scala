package playlistoptimizer.optimizer

import playlistoptimizer.Utils._
import playlistoptimizer.domain.{Key, Playlist}

import scala.util.Random

sealed trait Optimizer {
  def optimize: Playlist
}

case class GeneticAlgorithmOptimizer(initialPlaylist: Playlist, populationSize: Int, iterations: Int, mutationFactor: Double) extends Optimizer {
  private val random = new Random()
  private val playlistSize: Int = initialPlaylist.size
  private val initialPopulation: List[Playlist] = List.fill(populationSize)(randomizedPlaylist)

  override def optimize(): Playlist =
    (0 until populationSize)
      .foldLeft(initialPopulation)((currentPopulation, _) => singleIteration(currentPopulation))
      .head

  private def singleIteration(population: List[Playlist]): List[Playlist] = {
    val newPopulation = distributeInPairs(population)
      .flatMap { case (p1, p2) => List(crossover(p1, p2), crossover(p2, p1)) }
      .map(m => if (random.nextDouble < mutationFactor) mutate(m) else m)

    (newPopulation ++ population).sortBy(evaluate).take(populationSize)
  }

  private def distributeInPairs(population: List[Playlist]): List[(Playlist, Playlist)] = {
    val half = population.removeNth(2)
    half.zip(population.filterNot(half.contains))
  }

  private def randomizedPlaylist: Playlist = initialPlaylist.map(Random.shuffle(_))

  private def mutate(pl: Playlist): Playlist = pl.map(_.swap(random.nextInt(playlistSize), random.nextInt(playlistSize)))

  private def crossover(pl1: Playlist, pl2: Playlist): Playlist = pl1.zipWith(pl2)((s1, s2) => {
    val point1: Int = random.nextInt(playlistSize / 2)
    val point2: Int = random.nextInt(playlistSize / 2) + playlistSize / 2
    val (l, m, r) = s1.splitInThree(point1, point2)
    l ++ s2.filter(m.contains) ++ r
  })

  private def evaluate(pl: Playlist): Int = pl.reduce(_.sliding(2).foldLeft(0) { case (acc, s1 +: s2 +: _) => Key.distance(s1.key, s2.key) + acc })
}

package io.kirill.playlistoptimizer.core.optimizer.algorithms

import cats.effect.Concurrent
import cats.implicits._
import fs2.Stream
import io.kirill.playlistoptimizer.core.optimizer.OptimizationParameters
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators.{Crossover, Evaluator, Mutator}
import io.kirill.playlistoptimizer.core.utils.CollectionOps._

import scala.util.Random

class GeneticAlgorithm[F[_]: Concurrent, A: Crossover: Mutator: Evaluator](
    implicit val rand: Random
) extends OptimizationAlgorithm[F, A] {

  override def optimizeSeq(items: IndexedSeq[A], params: OptimizationParameters): F[(IndexedSeq[A], Double)] = {
    val initialPopulation = Seq.fill(params.populationSize)(if (params.shuffle) rand.shuffle(items) else items)
    Stream
      .range[F](0, params.maxGen)
      .evalScan(initialPopulation)((currPop, _) => singleIteration(currPop, params.mutationProbability))
      .compile
      .lastOrError
      .map(_.head)
      .map(res => (res, Evaluator[A].evaluate(res)))
  }

  private def singleIteration(population: Seq[IndexedSeq[A]], mutationFactor: Double)(implicit e: Evaluator[A]): F[Seq[IndexedSeq[A]]] = {
    val newPopulation = Stream
      .evalSeq(Concurrent[F].delay(population.pairs))
      .map {
        case (p1, p2) =>
          Stream.evalSeq(Concurrent[F].delay(List(breed(p1, p2, mutationFactor), breed(p2, p1, mutationFactor))))
      }
      .parJoinUnbounded

    val oldPopulation = Stream.evalSeq(population.pure[F])

    (newPopulation ++ oldPopulation).compile.toVector.map(_.sortBy(e.evaluate).take(population.size))
  }

  private def breed(
      p1: IndexedSeq[A],
      p2: IndexedSeq[A],
      mutationFactor: Double
  )(
    implicit c: Crossover[A],
    m: Mutator[A],
    r: Random
  ): IndexedSeq[A] = {
    val child = c.cross(p1, p2)(r = r)
    if (r.nextDouble < mutationFactor) m.mutate(child)(r = r) else child
  }
}

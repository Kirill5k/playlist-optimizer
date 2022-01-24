package io.kirill.playlistoptimizer.core.optimizer

import cats.effect.IO
import io.kirill.playlistoptimizer.core.optimizer.Optimizable.given
import io.kirill.playlistoptimizer.domain.CatsIOSpec
import io.kirill.playlistoptimizer.domain.optimization.OptimizationParameters
import io.kirill.playlistoptimizer.domain.playlist.{PlaylistBuilder, Track}
import io.kirill.playlistoptimizer.free.operators.*

import java.time.Instant
import scala.util.Random

class GeneticAlgorithmSpec extends CatsIOSpec {
  given random: Random = Random(1)

  "A GeneticAlgorithm" - {

    "should optimize a seq of tracks" in {
      val alg = OptimizationAlgorithm.geneticAlgorithm[IO, Track](
        Crossover.bestKeySequenceTrackCrossover,
        Mutator.randomSwapMutator[Track],
        Evaluator.harmonicSeqBasedTracksEvaluator,
        Selector.rouletteWheelSelector[Track],
        Elitism.simple[Track]
      )

      val start = Instant.now

      val playlist = PlaylistBuilder.playlist
      val params   = OptimizationParameters(200, 350, 0.75, 0.05, 0.25, true)

      val optimizedPlaylist = alg.optimize(playlist, params, (_, _) => IO.unit)

      optimizedPlaylist.asserting { case (result, score) =>
        val end = Instant.now()

        println(s"initial score: ${evalInd(playlist.repr)}")
        println(s"final score: $score")
        println(s"total time taken: ${end.getEpochSecond - start.getEpochSecond}s")

        result.tracks must contain theSameElementsAs playlist.tracks
        result.tracks must not contain theSameElementsInOrderAs(playlist.tracks)
        score must be < evalInd(playlist.repr) / 20
      }
    }
  }

  def evalInd(ind: Array[Track]): BigDecimal =
    Evaluator.harmonicSeqBasedTracksEvaluator.evaluateIndividual(ind)._2.value
}

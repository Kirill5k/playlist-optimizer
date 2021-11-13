package io.kirill.playlistoptimizer.core.optimizer.algorithms.operators

import io.kirill.playlistoptimizer.core.playlist.Key.*
import io.kirill.playlistoptimizer.core.playlist.{Key, Track}
import org.scalatest.Inspectors
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class EvaluatorSpec extends AnyWordSpec with Matchers with Inspectors {
  import io.kirill.playlistoptimizer.core.playlist.TrackBuilder.*

  "A harmonicSeqBasedTracksEvaluator" should {

    val evaluator = Evaluator.harmonicSeqBasedTracksEvaluator

    "evaluate a sequence of tracks based on keys position" in {
      val keysWithScore   = Map(
        List(BMajor, EMajor, EMajor, AMajor, DMajor, GMajor, GMajor, GMajor, GMajor, GMajor) -> 4,
        List(EMajor, GMinor, BFlatMinor, EMinor) -> 94
      )

      forAll(keysWithScore) {
        case (keys, expected) =>
          val tracks = tracksSeq(keys = keys)
          evaluator.evaluateIndividual(tracks) must be(Fitness(expected))
      }
    }
  }

  "energyBasedTracksEvaluator" should {

    val evaluator = Evaluator.energyFlowBasedTracksEvaluator

    "evaluate a sequence of tracks based on track's energy when danceability doesnt change" in {
      val energiesWithScore = Map(
        List(0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50, 0.55) -> 45,
        List(0.50, 0.45, 0.40, 0.35, 0.30, 0.35, 0.40, 0.45, 0.50, 0.55) -> 45,
        List(0.50, 0.49, 0.48, 0.47, 0.95, 0.45, 0.44, 0.43, 0.42, 0.41) -> 105
      )

      forAll(energiesWithScore) {
        case (en, expected) =>
          val tracks = tracksSeq(energies = en)
          evaluator.evaluateIndividual(tracks) must be(Fitness(expected))
      }
    }

    "evaluate a sequence of tracks based on track's danceability when energy doesnt change" in {
      val danceabilitiesWithScore = Map(
        List(0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50, 0.55) -> 45,
        List(0.50, 0.45, 0.40, 0.35, 0.30, 0.35, 0.40, 0.45, 0.50, 0.55) -> 45,
        List(0.50, 0.49, 0.48, 0.47, 0.95, 0.45, 0.44, 0.43, 0.42, 0.41) -> 105
      )

      forAll(danceabilitiesWithScore) {
        case (dance, expected) =>
          val tracks = tracksSeq(danceabilities = dance)
          evaluator.evaluateIndividual(tracks) must be(Fitness(expected))
      }
    }

    "evaluate a sequence of tracks based on track's danceability and energy" in {
      val danceabilitiesWithScore = Map(
        (
          List(0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50, 0.55),
          List(0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50, 0.55)
        ) -> 90
      )

      forAll(danceabilitiesWithScore) {
        case ((energies, danceabilities), expected) =>
          val tracks = tracksSeq(danceabilities = danceabilities, energies = energies)
          evaluator.evaluateIndividual(tracks) must be(Fitness(expected))
      }
    }
  }

  def tracksSeq(
      keys: List[Key] = Nil,
      energies: List[Double] = Nil,
      danceabilities: List[Double] = Nil
  ): Array[Track] = {
    keys.zipAll(energies, EMajor, 0.5).zipAll(danceabilities, (EMajor, 0.5), 0.5).zipWithIndex.map {
      case (((key, en), dance), i) => track(s"song $i", key = key, energy = en, danceability = dance)
    }.toArray
  }
}

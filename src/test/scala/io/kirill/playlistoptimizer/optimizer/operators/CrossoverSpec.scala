package io.kirill.playlistoptimizer.optimizer.operators

import io.kirill.playlistoptimizer.domain.Key._
import io.kirill.playlistoptimizer.domain.Track
import io.kirill.playlistoptimizer.domain.TrackBuilder.track
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

class CrossoverSpec extends AnyWordSpec with Matchers {



  "A keySequenceBasedTracksCrossover" should {

    "transfer best sequence of a parent 1 to a child" in {
      implicit val random = new Random(1)

      val p1 = Vector(
        track("s1", BMajor),
        track("s2", CMajor),
        track("s3", BMajor),
        track("s4", EMajor),
        track("s5", EFlatMajor),
        track("s6", AMinor),
        track("s7", DMinor),
        track("s8", DMinor),
        track("s9", GMinor),
        track("s10", CMinor),
        track("s11", GMajor),
        track("s12", GMajor)
      )

      val p2 = Random.shuffle(p1)

      val c = Crossover.keySequenceBasedTracksCrossover.cross(p1, p2)

      c must contain inOrder (track("s6", AMinor), track("s7", DMinor), track("s8", DMinor), track("s9", GMinor), track("s10", CMinor))
      c must contain theSameElementsAs p1
      c must contain theSameElementsAs p2
      c must not contain theSameElementsInOrderAs (p1)
      c must not contain theSameElementsInOrderAs (p2)
    }

    "slice best sequence of a parent 1 if it is too long" in {
      implicit val random = new Random(2)

      val p1 = Vector(
        track("s1", AMajor),
        track("s2", EMajor),
        track("s3", BMajor),
        track("s4", FSharpMajor),
        track("s5", DFlatMajor),
        track("s6", AFlatMajor),
        track("s7", EFlatMajor),
        track("s8", BFlatMajor),
        track("s9", FMajor),
        track("s10", CMajor),
        track("s11", GMajor),
        track("s12", DMajor)
      )

      val p2 = Random.shuffle(p1)

      val c = Crossover.keySequenceBasedTracksCrossover.cross(p1, p2)

      c must contain inOrder (track("s2", EMajor),track("s3", BMajor), track("s4", FSharpMajor), track("s5", DFlatMajor), track("s6", AFlatMajor), track("s7", EFlatMajor))
      c must contain theSameElementsAs p1
      c must contain theSameElementsAs p2
      c must not contain theSameElementsInOrderAs (p1)
      c must not contain theSameElementsInOrderAs (p2)
    }
  }

  "A threeWaySplitCrossover" should {

    "cross 2 parents into a child" in {
      implicit val random = new Random(1)

      val p1 = Vector(
        track("s1", BMajor),
        track("s2", EMajor),
        track("s3", EMajor),
        track("s4", AMajor),
        track("s5", DMajor),
        track("s6", GMajor),
        track("s7", GMajor),
        track("s8", GMajor),
        track("s9", GMajor),
        track("s10", GMajor)
      )

      val p2 = Random.shuffle(p1)
      val child = Crossover.threeWaySplitCrossover[Track].cross(p1, p2)

      child must contain theSameElementsAs p1
      child must contain theSameElementsAs p2
      child must not contain theSameElementsInOrderAs (p1)
      child must not contain theSameElementsInOrderAs (p2)
    }
  }
}

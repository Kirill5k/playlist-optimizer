package io.kirill.playlistoptimizer.playlist

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class KeySpec extends AnyWordSpec with Matchers {
  import Key._

  "A ket.distance" should {

    "calculate distance for keys with the same mode" in {
      Key.distance(DFlatMajor, BMajor) must be (2)
      Key.distance(FSharpMajor, GMajor) must be (5)
      Key.distance(CMajor, EMajor) must be (4)
      Key.distance(BFlatMajor, EMajor) must be (6)
      Key.distance(EFlatMajor, EMajor) must be (5)
      Key.distance(AFlatMinor, FSharpMinor) must be (2)
      Key.distance(FMajor, BMajor) must be (6)
      Key.distance(BMajor, FMajor) must be (6)
      Key.distance(BMajor, BMajor) must be (0)
      Key.distance(BMajor, EMajor) must be (1)
    }

    "calculate distance for keys with the different modes" in {
      Key.distance(AFlatMinor, BMajor) must be (1)
      Key.distance(AFlatMinor, EMajor) must be (2)
      Key.distance(EMinor, DFlatMajor) must be (7)
      Key.distance(EMinor, AMajor) must be (3)
      Key.distance(FMajor, AFlatMinor) must be (7)
      Key.distance(AFlatMinor, FMajor) must be (7)
    }
  }
}

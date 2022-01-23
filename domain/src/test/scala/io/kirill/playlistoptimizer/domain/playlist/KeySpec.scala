package io.kirill.playlistoptimizer.domain.playlist

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class KeySpec extends AnyWordSpec with Matchers {
  import Key.*

  "A ket.distance" should {

    "calculate distance for keys with the same mode" in {
      Key.distance(DFlatMajor, BMajor) mustBe 2
      Key.distance(FSharpMajor, GMajor) mustBe 5
      Key.distance(CMajor, EMajor) mustBe 4
      Key.distance(BFlatMajor, EMajor) mustBe 6
      Key.distance(EFlatMajor, EMajor) mustBe 5
      Key.distance(AFlatMinor, FSharpMinor) mustBe 2
      Key.distance(FMajor, BMajor) mustBe 6
      Key.distance(BMajor, FMajor) mustBe 6
      Key.distance(BMajor, BMajor) mustBe 0
      Key.distance(BMajor, EMajor) mustBe 1
    }

    "calculate distance for keys with the different modes" in {
      Key.distance(AFlatMinor, BMajor) mustBe 1
      Key.distance(AFlatMinor, EMajor) mustBe 2
      Key.distance(EMinor, DFlatMajor) mustBe 7
      Key.distance(EMinor, AMajor) mustBe 3
      Key.distance(FMajor, AFlatMinor) mustBe 7
      Key.distance(AFlatMinor, FMajor) mustBe 7
    }
  }
}

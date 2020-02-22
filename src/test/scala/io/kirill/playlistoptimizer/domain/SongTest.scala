package io.kirill.playlistoptimizer.domain

import org.scalatest.{FunSpec}

class SongTest extends FunSpec {
  describe("Key.distance") {
    it("should calculate distance for keys with the same mode") {
      assert(Key.distance(DFlatMajor, BMajor) === 2)
      assert(Key.distance(FSharpMajor, GMajor) === 5)
      assert(Key.distance(CMajor, EMajor) === 4)
      assert(Key.distance(BFlatMajor, EMajor) === 6)
      assert(Key.distance(EFlatMajor, EMajor) === 5)
      assert(Key.distance(AFlatMinor, FSharpMinor) === 2)
      assert(Key.distance(FMajor, BMajor) === 6)
      assert(Key.distance(BMajor, FMajor) === 6)
      assert(Key.distance(BMajor, BMajor) === 0)
      assert(Key.distance(BMajor, EMajor) === 1)
    }

    it("should calculate distance for keys with the different modes") {
      assert(Key.distance(AFlatMinor, BMajor) === 1)
      assert(Key.distance(AFlatMinor, EMajor) === 2)
      assert(Key.distance(EMinor, DFlatMajor) === 7)
      assert(Key.distance(EMinor, AMajor) === 3)
      assert(Key.distance(FMajor, AFlatMinor) === 7)
      assert(Key.distance(AFlatMinor, FMajor) === 7)
    }
  }
}

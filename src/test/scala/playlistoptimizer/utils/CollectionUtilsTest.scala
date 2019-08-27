package playlistoptimizer.utils

import org.scalatest.{FunSpec, Matchers}
import playlistoptimizer.utils.CollectionUtils._

class CollectionUtilsTest extends FunSpec with Matchers {
  describe("IndexedSeqUtils") {
    describe("swap") {
      it("swap") {
        val result = Vector(1, 2, 3, 4, 5, 6, 7).swap(2, 5)
        result should contain theSameElementsInOrderAs (Vector(1, 2, 6, 4, 5, 3, 7))
      }

      it("returns same vector when pos1 == pos2") {
        val result = Vector(1, 2, 3, 4, 5, 6, 7).swap(2, 2)
        result should contain theSameElementsInOrderAs (Vector(1, 2, 3, 4, 5, 6, 7))
      }

      it("throws exception when pos is greater than size") {
        the [IllegalArgumentException] thrownBy {
          Vector(1, 2, 3, 4, 5, 6, 7).swap(2, 10)
        } should have message "pos cannot be greater than size"
      }
    }

    describe("splitInThree") {
      it("splits vector in 3") {
        val (left, mid, right) = Vector(1, 2, 3, 4, 5, 6, 7).splitInThree(2, 5)
        left should contain theSameElementsInOrderAs (Vector(1, 2))
        mid should contain theSameElementsInOrderAs (Vector(3, 4))
        right should contain theSameElementsInOrderAs (Vector(5, 6, 7))
      }

      it("returns empty left when point1 is 0") {
        val (left, mid, right) = Vector(1, 2, 3, 4, 5, 6, 7).splitInThree(0, 7)
        left should contain theSameElementsInOrderAs (Vector())
        mid should contain theSameElementsInOrderAs (Vector(1, 2, 3, 4, 5, 6))
        right should contain theSameElementsInOrderAs (Vector(7))
      }

      it("throws exception when point1 is greater than point2") {
        the [IllegalArgumentException] thrownBy {
          Vector(1, 2, 3, 4, 5, 6, 7).splitInThree(10, 2)
        } should have message "point 1 must be less than point 2"
      }

      it("throws exception when point is greater than size") {
        the [IllegalArgumentException] thrownBy {
          Vector(1, 2, 3, 4, 5, 6, 7).splitInThree(2, 10)
        } should have message "points cannot be greater than size"
      }
    }
  }

  describe("ListUtils") {
    describe("removeNth") {
      it("removes every 2nd element") {
        val result = List(1, 2, 3, 4, 5, 6, 7, 8).removeNth(2)
        result should contain theSameElementsInOrderAs (List(1, 3, 5, 7))
      }

      it("removes every 3rd element") {
        val result = List(1, 2, 3, 4, 5, 6, 7, 8).removeNth(3)
        result should contain theSameElementsInOrderAs (List(1, 2, 4, 5, 7, 8))
      }
    }
  }
}

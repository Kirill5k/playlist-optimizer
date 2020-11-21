package io.kirill.playlistoptimizer.core.utils

import collections._
import org.scalatest.Inspectors
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CollectionOpsSpec extends AnyWordSpec with Matchers with Inspectors {

  "SeqOps" should {
    "split index seq in 3" in {
      val (left, mid, right) = Vector(1, 2, 3, 4, 5, 6, 7).splitInThree(2, 5)
      left must contain theSameElementsInOrderAs (List(1, 2))
      mid must contain theSameElementsInOrderAs (List(3, 4))
      right must contain theSameElementsInOrderAs (List(5, 6, 7))
    }

    "return empty left when point1 is 0" in {
      val (left, mid, right) = Vector(1, 2, 3, 4, 5, 6, 7).splitInThree(0, 7)
      left must contain theSameElementsInOrderAs (List())
      mid must contain theSameElementsInOrderAs (List(1, 2, 3, 4, 5, 6))
      right must contain theSameElementsInOrderAs (List(7))
    }

    "throws exception when point1 is greater than point2" in {
      the [IllegalArgumentException] thrownBy {
        Vector(1, 2, 3, 4, 5, 6, 7).splitInThree(10, 2)
      } must have message "point 1 must be less than point 2"
    }

    "throws exception when point is greater than size" in {
      the [IllegalArgumentException] thrownBy {
        Vector(1, 2, 3, 4, 5, 6, 7).splitInThree(2, 10)
      } must have message "points cannot be greater than size"
    }

    "distributes list elements in pairs" in {
      val result = List(1, 2, 3, 4, 5, 6, 7, 8).pairs
      result must be (List((1, 2), (3, 4), (5, 6), (7, 8)))
    }

    "swap elements in indexed seq" in {
      val result = Vector(1, 2, 3, 4, 5, 6, 7).swap(2, 5)
      result must contain theSameElementsInOrderAs (Vector(1, 2, 6, 4, 5, 3, 7))
    }

    "return same vector when pos1 == pos2" in {
      val result = Vector(1, 2, 3, 4, 5, 6, 7).swap(2, 2)
      result must contain theSameElementsInOrderAs (Vector(1, 2, 3, 4, 5, 6, 7))
    }

    "throw exception when pos is greater than size" in {
      the [IllegalArgumentException] thrownBy {
        Vector(1, 2, 3, 4, 5, 6, 7).swap(2, 10)
      } must have message "pos cannot be greater than size"
    }
  }
}

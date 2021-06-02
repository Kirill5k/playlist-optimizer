package io.kirill.playlistoptimizer.core.utils

import collections._
import org.scalatest.Inspectors
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CollectionOpsSpec extends AnyWordSpec with Matchers with Inspectors {

  "SeqOps" should {
    "distributes list elements in pairs" in {
      val result = List(1, 2, 3, 4, 5, 6, 7, 8).pairs
      result must be (List((1, 2), (3, 4), (5, 6), (7, 8)))
    }
  }
}

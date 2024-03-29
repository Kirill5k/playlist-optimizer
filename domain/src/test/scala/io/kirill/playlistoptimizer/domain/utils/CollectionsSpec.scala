package io.kirill.playlistoptimizer.domain.utils

import io.kirill.playlistoptimizer.domain.utils.collections.*
import org.scalatest.Inspectors
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CollectionsSpec extends AnyWordSpec with Matchers with Inspectors {

  import collections.*

  "Seq" should {
    "distribute list elements in pairs" in {
      val result = List(1, 2, 3, 4, 5, 6, 7, 8).pairs
      result mustBe List((1, 2), (3, 4), (5, 6), (7, 8))
    }
  }
}

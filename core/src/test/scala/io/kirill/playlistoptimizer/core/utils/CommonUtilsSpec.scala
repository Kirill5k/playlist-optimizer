package io.kirill.playlistoptimizer.core.utils

import common._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CommonUtilsSpec extends AnyWordSpec with Matchers {

  "A BooleanModifier" should {
    "convert boolean to int" in {
      true.toInt must be (1)
      false.toInt must be (0)
    }
  }
}

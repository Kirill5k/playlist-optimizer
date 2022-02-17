package io.kirill.playlistoptimizer.domain.utils

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CommonSpec extends AnyWordSpec with Matchers {

  import common.*

  "A BooleanModifier" should {
    "convert boolean to int" in {
      true.toInt must be(1)
      false.toInt must be(0)
    }
  }
}

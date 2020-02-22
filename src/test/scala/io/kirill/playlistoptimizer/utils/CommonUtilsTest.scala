package io.kirill.playlistoptimizer.utils

import org.scalatest.{FunSpec, Matchers}
import CommonUtils._

class CommonUtilsTest extends FunSpec with Matchers {
  describe("BooleanModifier") {
    describe("toInt") {
      it("converts boolean to int") {
        assert(true.toInt === 1)
        assert(false.toInt === 0)
      }
    }
  }
}

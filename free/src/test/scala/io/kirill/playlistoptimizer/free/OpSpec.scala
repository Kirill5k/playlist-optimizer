package io.kirill.playlistoptimizer.free

import cats.free.Free
import cats.effect.IO
import io.kirill.playlistoptimizer.domain.{CatsIOSpec, MockitoMatchers}
import io.kirill.playlistoptimizer.free.operators.{Crossover, Elitism, Evaluator, Mutator, Selector}

import scala.util.Random

class OpSpec extends CatsIOSpec with MockitoMatchers {

  "An io interpreter when" - {
    "mutate is submitted should" - {
      "apply mutation op an array" in {
        given r: Random                 = Random(42)
        val (cross, _, eval, sel, elit) = mocks[Int]
        val interpreter                 = Op.ioInterpreter[IO, Int](cross, Mutator.randomSwapMutator[Int], eval, sel, elit)

        Op.Mutate[Int](Array.range(0, 10), 0.2)
          .freeM
          .foldMap(interpreter)
          .asserting(_ mustBe Array(3, 1, 2, 0, 4, 5, 6, 7, 8, 9))
      }

      "apply mutation operation on array 3 times in the row" in {
        given r: Random                 = Random(42)
        val (cross, _, eval, sel, elit) = mocks[Int]
        val interpreter                 = Op.ioInterpreter[IO, Int](cross, Mutator.randomSwapMutator[Int], eval, sel, elit)

        val result = for
          i1 <- Op.Mutate(Array.range(0, 10), 0.2).freeM
          i2 <- Op.Mutate(i1, 0.2).freeM
          i3 <- Op.Mutate(i2, 0.2).freeM
        yield i3

        result
          .foldMap(interpreter)
          .asserting(_ mustBe Array(5, 1, 2, 0, 8, 3, 6, 7, 4, 9))
      }
    }
  }

  def mocks[G]: (Crossover[G], Mutator[G], Evaluator[G], Selector[G], Elitism[G]) =
    (mock[Crossover[G]], mock[Mutator[G]], mock[Evaluator[G]], mock[Selector[G]], mock[Elitism[G]])
}

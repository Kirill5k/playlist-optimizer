package io.kirill.playlistoptimizer.free

import cats.free.Free
import cats.effect.IO
import io.kirill.playlistoptimizer.domain.{CatsIOSpec, MockitoMatchers}
import io.kirill.playlistoptimizer.free.operators.{Crossover, Elitism, Evaluator, Mutator, Selector}
import io.kirill.playlistoptimizer.domain.utils.collections.*
import org.mockito.Mockito.times

import scala.util.Random

class OpSpec extends CatsIOSpec with MockitoMatchers {

  "An IO interpreter should" - {

    given r: Random = Random(42)

    val ind1 = Array.range(0, 10)
    val ind2 = Array.range(0, 10).shuffle

    "mutate an individual when" - {
      "Op.Mutate is submitted" in {
        val (cross, mut, eval, sel, elit) = mocks[Int]
        val interpreter                   = Op.ioInterpreter[IO, Int](cross, mut, eval, sel, elit)

        when(mut.mutate(any[Ind[Int]], any[Double])(using eqTo(r))).thenReturn(ind2)

        Op.Mutate[Int](Array.range(0, 10), 0.2)
          .freeM
          .foldMap(interpreter)
          .asserting { res =>
            verify(mut).mutate(ind1, 0.2)
            res mustBe ind2
          }
      }

      "Op.Mutate is submitted 3 times in the row" in {
        val (cross, mut, eval, sel, elit) = mocks[Int]
        val interpreter                   = Op.ioInterpreter[IO, Int](cross, mut, eval, sel, elit)

        when(mut.mutate(any[Ind[Int]], any[Double])(using eqTo(r)))
          .thenReturn(ind1)
          .thenReturn(ind1)
          .thenReturn(ind2)

        val result = for
          i1 <- Op.Mutate(ind1, 0.2).freeM
          i2 <- Op.Mutate(i1, 0.2).freeM
          i3 <- Op.Mutate(i2, 0.2).freeM
        yield i3

        result
          .foldMap(interpreter)
          .asserting { res =>
            verify(mut, times(3)).mutate(any[Array[Int]], eqTo(0.2))(using eqTo(r))
            res mustBe ind2
          }
      }
    }

    "evaluate entire population when" - {
      "Op.EvaluatePopulation is submitted" in {
        val (cross, mut, eval, sel, elit) = mocks[Int]
        val interpreter                   = Op.ioInterpreter[IO, Int](cross, mut, eval, sel, elit)

        when(eval.evaluateIndividual(any[Ind[Int]])).thenAnswer(a => (a.getArgument(0), Fitness(1)))

        Op.EvaluatePopulation(List.fill(10)(ind1.shuffle))
          .freeM
          .foldMap(interpreter)
          .asserting { res =>
            verify(eval, times(10)).evaluateIndividual(any[Array[Int]])
            res must have size 10
          }
      }
    }
  }

  def mocks[G]: (Crossover[G], Mutator[G], Evaluator[G], Selector[G], Elitism[G]) =
    (mock[Crossover[G]], mock[Mutator[G]], mock[Evaluator[G]], mock[Selector[G]], mock[Elitism[G]])
}

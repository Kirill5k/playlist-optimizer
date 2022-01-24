package io.kirill.playlistoptimizer.core.optimizer

import cats.effect.Async
import cats.syntax.functor.*
import io.kirill.playlistoptimizer.core.optimizer.Optimizable
import io.kirill.playlistoptimizer.domain.optimization.OptimizationParameters
import io.kirill.playlistoptimizer.free.operators.*
import io.kirill.playlistoptimizer.free.{Algorithm, Op}

import scala.reflect.ClassTag
import scala.util.Random

trait OptimizationAlgorithm[F[_], A]:
  def optimize[T](
      target: T,
      parameters: OptimizationParameters,
      updateProgress: BigDecimal => F[Unit]
  )(using
      optimizable: Optimizable[T, A],
      rand: Random
  ): F[(T, BigDecimal)]

object OptimizationAlgorithm:
  inline def geneticAlgorithm[F[_]: Async, A: ClassTag](
      crossover: Crossover[A],
      mutator: Mutator[A],
      evaluator: Evaluator[A],
      selector: Selector[A],
      elitism: Elitism[A]
  ): OptimizationAlgorithm[F, A] = new OptimizationAlgorithm[F, A] {
    def optimize[T](
        target: T,
        parameters: OptimizationParameters,
        updateProgress: BigDecimal => F[Unit]
    )(using
        optimizable: Optimizable[T, A],
        rand: Random
    ): F[(T, BigDecimal)] =
      Algorithm
        .GeneticAlgorithm
        .optimize[A](target.repr, parameters)
        .foldMap(Op.ioInterpreter[F, A](crossover, mutator, evaluator, selector, elitism))
        .map((res, f) => (target.update(res), f.value))
  }

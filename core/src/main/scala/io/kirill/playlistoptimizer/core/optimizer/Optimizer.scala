package io.kirill.playlistoptimizer.core.optimizer

import java.util.UUID

import cats.effect._
import cats.implicits._
import fs2.Stream
import io.kirill.playlistoptimizer.core.common.config.GeneticAlgorithmConfig
import io.kirill.playlistoptimizer.core.optimizer.Optimizer.OptimizationResult
import io.kirill.playlistoptimizer.core.optimizer.algorithms.Algorithm
import io.kirill.playlistoptimizer.core.optimizer.operators.{Crossover, Evaluator, Mutator}
import io.kirill.playlistoptimizer.core.optimizer.operators.Crossover
import io.kirill.playlistoptimizer.core.utils.CollectionOps._

import scala.util.Random

trait Optimizer[F[_], A] {
  def optimize(items: Seq[A])(implicit alg: Algorithm[F, A]): F[Seq[A]]

  def get(id: UUID): F[OptimizationResult[A]]
}

object Optimizer {
  final case class OptimizationResult[A](id: UUID, result: Option[A])
}

package io.kirill.playlistoptimizer.core.optimizer

import cats.effect.implicits._
import cats.effect.{Concurrent, Ref, Temporal}
import cats.implicits._
import io.kirill.playlistoptimizer.core.common.controllers.Controller.UserSessionId
import io.kirill.playlistoptimizer.core.common.errors.OptimizationNotFound
import io.kirill.playlistoptimizer.core.optimizer.algorithms.{Optimizable, OptimizationAlgorithm}
import io.kirill.playlistoptimizer.core.playlist.Track

import scala.concurrent.duration._
import scala.util.Random

trait Optimizer[F[_], A] {
  def optimize(uid: UserSessionId, target: Optimizable[A], parameters: OptimizationParameters): F[OptimizationId]
  def get(uid: UserSessionId, id: OptimizationId): F[Optimization[A]]
  def getAll(uid: UserSessionId): F[List[Optimization[A]]]
  def delete(uid: UserSessionId, id: OptimizationId): F[Unit]
}

private class InmemoryOptimizer[F[_]: Concurrent, A](
    private val state: Ref[F, Map[UserSessionId, Map[OptimizationId, Optimization[A]]]],
    private val alg: OptimizationAlgorithm[F, A]
)(implicit
    r: Random = new Random()
) extends Optimizer[F, A] {

  override def get(uid: UserSessionId, oid: OptimizationId): F[Optimization[A]] =
    state.get
      .map(_.get(uid).flatMap(_.get(oid)))
      .flatMap {
        case None      => OptimizationNotFound(oid).raiseError[F, Optimization[A]]
        case Some(opt) => opt.pure[F]
      }

  override def getAll(uid: UserSessionId): F[List[Optimization[A]]] =
    state.get.map(_.get(uid).fold(List.empty[Optimization[A]])(_.values.toList))

  override def optimize(uid: UserSessionId, optimizable: Optimizable[A], parameters: OptimizationParameters): F[OptimizationId] =
    for {
      oid <- save(uid, Optimization.init[A](parameters, optimizable))
      process = alg.optimize(optimizable, parameters).flatMap(res => complete(uid, oid, res._1, res._2))
      _ <- process.start.void
    } yield oid

  private def complete(uid: UserSessionId, id: OptimizationId, result: Array[A], score: BigDecimal): F[Unit] =
    get(uid, id).flatMap(opt => save(uid, opt.complete(result, score))).void

  private def save(uid: UserSessionId, opt: Optimization[A]): F[OptimizationId] =
    state.update(s => s + (uid -> (s.getOrElse(uid, Map.empty) + (opt.id -> opt)))) *>
      opt.id.pure[F]

  override def delete(uid: UserSessionId, id: OptimizationId): F[Unit] =
    state.get.flatMap {
      case s if s.get(uid).exists(_.contains(id)) => state.update(curr => curr + (uid -> curr(uid).removed(id)))
      case _                                      => OptimizationNotFound(id).raiseError[F, Unit]
    }
}

object Optimizer {

  def inmemoryPlaylistOptimizer[F[_]: Temporal](
      alg: OptimizationAlgorithm[F, Track],
      expiresIn: FiniteDuration = 24.hours,
      checkOnExpirationsEvery: FiniteDuration = 15.minutes
  ): F[Optimizer[F, Track]] = {
    def runExpiration(state: Ref[F, Map[UserSessionId, Map[OptimizationId, Optimization[Track]]]]): F[Unit] = {
      def expire: F[Unit] = state.update(_.foldLeft(Map.empty[UserSessionId, Map[OptimizationId, Optimization[Track]]]) {
        case (res, (uid, optsMap)) =>
          res + (uid -> optsMap.filter { case (_, opt) => opt.hasCompletedLessThan(expiresIn) })
      })
      Temporal[F].sleep(checkOnExpirationsEvery) >> expire >> runExpiration(state)
    }

    Ref
      .of[F, Map[UserSessionId, Map[OptimizationId, Optimization[Track]]]](Map.empty)
      .flatTap(s => runExpiration(s).start.void)
      .map(s => new InmemoryOptimizer[F, Track](s, alg))
  }
}

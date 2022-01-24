package io.kirill.playlistoptimizer.core.optimizer

import cats.effect.implicits.*
import cats.effect.{Concurrent, Ref, Temporal}
import cats.implicits.*
import io.kirill.playlistoptimizer.core.common.controllers.Controller.UserSessionId
import io.kirill.playlistoptimizer.domain.errors.OptimizationNotFound
import io.kirill.playlistoptimizer.domain.optimization.*
import io.kirill.playlistoptimizer.domain.playlist.{Playlist, Track}

import scala.concurrent.duration.*
import scala.util.Random

trait Optimizer[F[_], T, A] {
  def optimize(uid: UserSessionId, target: T, parameters: OptimizationParameters): F[OptimizationId]
  def get(uid: UserSessionId, id: OptimizationId): F[Optimization[T]]
  def getAll(uid: UserSessionId): F[List[Optimization[T]]]
  def delete(uid: UserSessionId, id: OptimizationId): F[Unit]
}

private class InmemoryOptimizer[F[_]: Concurrent, T, A](
    private val state: Ref[F, Map[UserSessionId, Map[OptimizationId, Optimization[T]]]],
    private val alg: OptimizationAlgorithm[F, A]
)(implicit
    optimizable: Optimizable[T, A],
    r: Random = new Random()
) extends Optimizer[F, T, A] {

  override def get(uid: UserSessionId, oid: OptimizationId): F[Optimization[T]] =
    state.get
      .map(_.get(uid).flatMap(_.get(oid)))
      .flatMap {
        case None      => OptimizationNotFound(oid).raiseError[F, Optimization[T]]
        case Some(opt) => opt.pure[F]
      }

  override def getAll(uid: UserSessionId): F[List[Optimization[T]]] =
    state.get.map(_.get(uid).fold(List.empty[Optimization[T]])(_.values.toList))

  override def optimize(uid: UserSessionId, target: T, parameters: OptimizationParameters): F[OptimizationId] =
    for {
      oid <- save(uid, Optimization.init[T](parameters, target))
      process = alg.optimize(target, parameters, p => updateProgress(uid, oid, p)).flatMap(res => complete(uid, oid, res))
      _ <- process.start.void
    } yield oid

  private def updateProgress(uid: UserSessionId, id: OptimizationId, progress: BigDecimal): F[Unit] =
    get(uid, id).flatMap(opt => save(uid, opt.copy(progress = progress))).void

  private def complete(uid: UserSessionId, id: OptimizationId, result: (T, BigDecimal)): F[Unit] =
    get(uid, id).flatMap(opt => save(uid, opt.complete(result._1, result._2))).void

  private def save(uid: UserSessionId, opt: Optimization[T]): F[OptimizationId] =
    state.update(s => s + (uid -> (s.getOrElse(uid, Map.empty) + (opt.id -> opt)))) *>
      opt.id.pure[F]

  override def delete(uid: UserSessionId, id: OptimizationId): F[Unit] =
    state.modify {
      case s if s.get(uid).exists(_.contains(id)) => (s + (uid -> s(uid).removed(id)), ().pure[F])
      case s                                      => (s, OptimizationNotFound(id).raiseError[F, Unit])
    }.flatten
}

object Optimizer {

  def inmemoryOptimizer[F[_]: Temporal, Target, Gene](
      alg: OptimizationAlgorithm[F, Gene],
      expiresIn: FiniteDuration = 24.hours,
      checkOnExpirationsEvery: FiniteDuration = 15.minutes
  )(using optimizable: Optimizable[Target, Gene]): F[Optimizer[F, Target, Gene]] = {
    def runExpiration(state: Ref[F, Map[UserSessionId, Map[OptimizationId, Optimization[Target]]]]): F[Unit] = {
      def expire: F[Unit] = state.update(_.foldLeft(Map.empty[UserSessionId, Map[OptimizationId, Optimization[Target]]]) {
        case (res, (uid, optsMap)) =>
          res + (uid -> optsMap.filter { case (_, opt) => !opt.isExpired(expiresIn) })
      })
      Temporal[F].sleep(checkOnExpirationsEvery) >> expire >> runExpiration(state)
    }

    Ref
      .of[F, Map[UserSessionId, Map[OptimizationId, Optimization[Target]]]](Map.empty)
      .flatTap(s => runExpiration(s).start.void)
      .map(s => InmemoryOptimizer[F, Target, Gene](s, alg))
  }

  def inmemoryPlaylistOptimizer[F[_]: Temporal](
      alg: OptimizationAlgorithm[F, Track],
      expiresIn: FiniteDuration = 24.hours,
      checkOnExpirationsEvery: FiniteDuration = 15.minutes
  ): F[Optimizer[F, Playlist, Track]] =
    inmemoryOptimizer[F, Playlist, Track](alg, expiresIn, checkOnExpirationsEvery)
}

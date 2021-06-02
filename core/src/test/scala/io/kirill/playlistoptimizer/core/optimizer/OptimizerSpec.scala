package io.kirill.playlistoptimizer.core.optimizer

import java.util.UUID
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits._
import io.kirill.playlistoptimizer.core.common.controllers.Controller.UserSessionId
import io.kirill.playlistoptimizer.core.common.errors.OptimizationNotFound
import io.kirill.playlistoptimizer.core.optimizer.algorithms.{Optimizable, OptimizationAlgorithm}
import io.kirill.playlistoptimizer.core.playlist.{Playlist, PlaylistBuilder, Track}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.duration._
import scala.util.Random

class OptimizerSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val random = new Random(1)

  val playlist        = PlaylistBuilder.playlist
  val optimizedTracks = random.shuffle(playlist.tracks).toArray

  val optimizationId = OptimizationId(UUID.randomUUID())
  val parameters     = OptimizationParameters(100, 1000, 0.5, 0.2, 0.1, true)

  val userSessionId = UserSessionId("user-session-id")

  "A InmemoryPlaylistOptimizer" - {
    implicit val plOpt = Optimizable.playlistOptimizable

    "initiate optimization of a playlist" in {
      val alg = mockAlg(IO.sleep(10.seconds) *> IO.pure((plOpt.update(playlist)(optimizedTracks), 25.0)))

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
      } yield id

      result.asserting(_ mustBe an[OptimizationId])
    }

    "return error when optimization id is not recognized" in {
      val alg = mockAlg(???)

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        res       <- optimizer.get(userSessionId, optimizationId)
      } yield res

      result.assertThrows[OptimizationNotFound]
    }

    "return error when user id is not recognized" in {
      val alg = mockAlg(IO.sleep(2.seconds) *> IO.pure((plOpt.update(playlist)(optimizedTracks), 25.0)))

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
        res       <- optimizer.get(UserSessionId("foo"), id)
      } yield res

      result.assertThrows[OptimizationNotFound]
    }

    "return incomplete optimization result after if it has not completed" in {
      val alg = mockAlg(IO.sleep(2.seconds) *> IO.pure((plOpt.update(playlist)(optimizedTracks), 25.0)))

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
        res       <- optimizer.get(userSessionId, id)
      } yield res

      result.asserting { optimization =>
        optimization.status must be("in progress")
        optimization.original must be(playlist)
        optimization.result must be(None)
        optimization.score must be(None)
      }
    }

    "return optimization result after it has completed" in {
      val alg = mockAlg(IO.sleep(2.seconds) *> IO.pure((plOpt.update(playlist)(optimizedTracks), 25.0)))
      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
        _         <- IO.sleep(3.seconds)
        res       <- optimizer.get(userSessionId, id)
      } yield res

      result.asserting { optimization =>
        optimization.status must be("completed")
        optimization.original must be(playlist)
        optimization.result must be(Some(plOpt.update(playlist)(optimizedTracks)))
        optimization.score must be(Some(25.0))
      }
    }

    "return all optimizations" in {
      val alg = mockAlg(IO.sleep(2.seconds) *> IO.pure((plOpt.update(playlist)(optimizedTracks), 25.0)))

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        _         <- optimizer.optimize(userSessionId, playlist, parameters)
        _         <- optimizer.optimize(userSessionId, playlist, parameters)
        res       <- optimizer.getAll(userSessionId)
      } yield res

      result.asserting { optimizations =>
        optimizations.size must be(2)
      }
    }

    "delete optimization" in {
      val alg = mockAlg(IO.sleep(2.seconds) *> IO.pure((plOpt.update(playlist)(optimizedTracks), 25.0)))

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
        _         <- optimizer.delete(userSessionId, id)
        res       <- optimizer.getAll(userSessionId)
      } yield res

      result.asserting(_ must be(Nil))
    }

    "return error if deleted optimization does not exist" in {
      val alg = mockAlg(???)

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        _         <- optimizer.delete(userSessionId, optimizationId)
        res       <- optimizer.getAll(userSessionId)
      } yield res

      result.assertThrows[OptimizationNotFound]
    }

    "return error if user id does not match" in {
      val alg = mockAlg(IO.sleep(2.seconds) *> IO.pure((plOpt.update(playlist)(optimizedTracks), 25.0)))

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
        res       <- optimizer.delete(UserSessionId("foo"), id)
      } yield res

      result.assertThrows[OptimizationNotFound]
    }

    "expired old optimizations" in {
      val alg = mockAlg(IO.pure((plOpt.update(playlist)(optimizedTracks), 25.0)))

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg, 5.seconds, 1.second)
        _         <- optimizer.optimize(userSessionId, playlist, parameters)
        _         <- IO.sleep(4.seconds)
        _         <- optimizer.optimize(userSessionId, playlist, parameters)
        _         <- IO.sleep(2.seconds)
        res       <- optimizer.getAll(userSessionId)
      } yield res

      result.asserting { optimizations =>
        optimizations.size must be(1)
      }
    }

    def mockAlg(returnResult: => IO[(Playlist, BigDecimal)]): OptimizationAlgorithm[IO, Track] =
      new OptimizationAlgorithm[IO, Track] {
        override def optimize[T](
            target: T,
            parameters: OptimizationParameters
        )(implicit
          optimizable: Optimizable[T, Track],
          r: Random
        ): IO[(T, BigDecimal)] =
          returnResult.map { case (res, score) => (res.asInstanceOf[T], score) }
      }
  }
}

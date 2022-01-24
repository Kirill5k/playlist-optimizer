package io.kirill.playlistoptimizer.core.optimizer

import java.util.UUID
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits.*
import io.kirill.playlistoptimizer.core.common.controllers.Controller.UserSessionId
import io.kirill.playlistoptimizer.domain.errors.OptimizationNotFound
import io.kirill.playlistoptimizer.domain.optimization.*
import io.kirill.playlistoptimizer.domain.playlist.{Playlist, PlaylistBuilder, Track}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.duration.*
import scala.util.Random

class OptimizerSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val random: Random = new Random(1)

  val playlist        = PlaylistBuilder.playlist
  val optimizedTracks = random.shuffle(playlist.tracks).toArray

  val optimizationId = OptimizationId.gen
  val parameters     = OptimizationParameters(100, 1000, 0.5, 0.2, 0.1, true)

  val userSessionId = UserSessionId("user-session-id")

  "A InmemoryPlaylistOptimizer" - {
    implicit val plOpt = Optimizable.playlistOptimizable

    "initiate optimization of a playlist" in {
      val alg = mockAlg(10.seconds)(plOpt.update(playlist)(optimizedTracks), 25.0)

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
      } yield id

      result.asserting(_ mustBe an[OptimizationId])
    }

    "return error when optimization id is not recognized" in {
      val alg = mockAlg(0.seconds)(null, null)

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        res       <- optimizer.get(userSessionId, optimizationId)
      } yield res

      result.assertThrows[OptimizationNotFound]
    }

    "return error when user id is not recognized" in {
      val alg = mockAlg(2.seconds)(plOpt.update(playlist)(optimizedTracks), 25.0)

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
        res       <- optimizer.get(UserSessionId("foo"), id)
      } yield res

      result.assertThrows[OptimizationNotFound]
    }

    "return incomplete optimization result after if it has not completed" in {
      val alg = mockAlg(5.seconds)(plOpt.update(playlist)(optimizedTracks), 25.0)

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
        _         <- IO.sleep(2.seconds)
        res       <- optimizer.get(userSessionId, id)
      } yield res

      result.asserting { optimization =>
        optimization.progress mustBe BigDecimal(20)
        optimization.original mustBe playlist
        optimization.result mustBe None
        optimization.score mustBe None
      }
    }

    "return optimization result after it has completed" in {
      val alg = mockAlg(2.seconds)(plOpt.update(playlist)(optimizedTracks), 25.0)
      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
        _         <- IO.sleep(3.seconds)
        res       <- optimizer.get(userSessionId, id)
      } yield res

      result.asserting { optimization =>
        optimization.progress mustBe BigDecimal(100)
        optimization.original mustBe playlist
        optimization.result mustBe (Some(plOpt.update(playlist)(optimizedTracks)))
        optimization.score mustBe (Some(25.0))
      }
    }

    "return all optimizations" in {
      val alg = mockAlg(2.seconds)(plOpt.update(playlist)(optimizedTracks), 25.0)

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        _         <- optimizer.optimize(userSessionId, playlist, parameters)
        _         <- optimizer.optimize(userSessionId, playlist, parameters)
        res       <- optimizer.getAll(userSessionId)
      } yield res

      result.asserting { optimizations =>
        optimizations.size mustBe 2
      }
    }

    "delete optimization" in {
      val alg = mockAlg(2.seconds)(plOpt.update(playlist)(optimizedTracks), 25.0)

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
        _         <- optimizer.delete(userSessionId, id)
        res       <- optimizer.getAll(userSessionId)
      } yield res

      result.asserting(_ mustBe Nil)
    }

    "return error if deleted optimization does not exist" in {
      val alg = mockAlg(0.seconds)(null, null)

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        _         <- optimizer.delete(userSessionId, optimizationId)
        res       <- optimizer.getAll(userSessionId)
      } yield res

      result.assertThrows[OptimizationNotFound]
    }

    "return error if user id does not match" in {
      val alg = mockAlg(2.seconds)(plOpt.update(playlist)(optimizedTracks), 25.0)

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg)
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
        res       <- optimizer.delete(UserSessionId("foo"), id)
      } yield res

      result.assertThrows[OptimizationNotFound]
    }

    "expired old optimizations" in {
      val alg = mockAlg(0.seconds)(plOpt.update(playlist)(optimizedTracks), 25.0)

      val result = for {
        optimizer <- Optimizer.inmemoryPlaylistOptimizer[IO](alg, 5.seconds, 1.second)
        _         <- optimizer.optimize(userSessionId, playlist, parameters)
        _         <- IO.sleep(4.seconds)
        _         <- optimizer.optimize(userSessionId, playlist, parameters)
        _         <- IO.sleep(2.seconds)
        res       <- optimizer.getAll(userSessionId)
      } yield res

      result.asserting { optimizations =>
        optimizations.size mustBe 1
      }
    }

    def mockAlg(duration: FiniteDuration)(returnResult: Playlist, score: BigDecimal): OptimizationAlgorithm[IO, Track] =
      new OptimizationAlgorithm[IO, Track] {
        override def optimize[T](
            target: T,
            parameters: OptimizationParameters,
            updateProgress: (Int, Int) => IO[Unit]
        )(implicit
            optimizable: Optimizable[T, Track],
            r: Random
        ): IO[(T, BigDecimal)] =
          (0 until duration.toSeconds.toInt).toList
            .traverse(i => updateProgress(i, duration.toSeconds.toInt) *> IO.sleep(1.second))
            .as((returnResult.asInstanceOf[T], score))
      }
  }
}

package io.kirill.playlistoptimizer.core.optimizer

import java.util.UUID

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits._
import io.kirill.playlistoptimizer.core.common.controllers.AppController.UserSessionId
import io.kirill.playlistoptimizer.core.common.errors.OptimizationNotFound
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.{PlaylistBuilder, Track}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.duration._
import scala.util.Random

class PlaylistOptimizerSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val random = new Random(1)

  val playlist        = PlaylistBuilder.playlist
  val optimizedTracks = random.shuffle(playlist.tracks)

  val optimizationId = OptimizationId(UUID.randomUUID())
  val parameters     = OptimizationParameters(100, 1000, 0.5, 0.2, 0.1, true)

  val userSessionId = UserSessionId("user-session-id")

  "A RefBasedPlaylistOptimizer" - {

    "initiate optimization of a playlist" in {
      implicit val alg = mockAlg(IO.sleep(10.seconds) *> IO.pure((optimizedTracks, 25.0)))

      val result = for {
        optimizer <- PlaylistOptimizer.refBasedPlaylistOptimizer[IO]
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
      } yield id

      result.asserting(_ mustBe an[OptimizationId])
    }

    "return error when optimization id is not recognized" in {
      implicit val alg = mockAlg(???)

      val result = for {
        optimizer <- PlaylistOptimizer.refBasedPlaylistOptimizer[IO]
        res       <- optimizer.get(userSessionId, optimizationId)
      } yield res

      result.assertThrows[OptimizationNotFound]
    }

    "return error when user id is not recognized" in {
      implicit val alg = mockAlg(IO.sleep(2.seconds) *> IO.pure((optimizedTracks, 25.0)))

      val result = for {
        optimizer <- PlaylistOptimizer.refBasedPlaylistOptimizer[IO]
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
        res       <- optimizer.get(UserSessionId("foo"), id)
      } yield res

      result.assertThrows[OptimizationNotFound]
    }

    "return incomplete optimization result after if it has not completed" in {
      implicit val alg = mockAlg(IO.sleep(2.seconds) *> IO.pure((optimizedTracks, 25.0)))

      val result = for {
        optimizer <- PlaylistOptimizer.refBasedPlaylistOptimizer[IO]
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
      implicit val alg = mockAlg(IO.sleep(2.seconds) *> IO.pure((optimizedTracks, 25.0)))
      val result = for {
        optimizer <- PlaylistOptimizer.refBasedPlaylistOptimizer[IO]
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
        _         <- IO.sleep(3.seconds)
        res       <- optimizer.get(userSessionId, id)
      } yield res

      result.asserting { optimization =>
        optimization.status must be("completed")
        optimization.original must be(playlist)
        optimization.result must be(Some(playlist.copy(tracks = optimizedTracks, name = s"Mel optimized")))
        optimization.score must be(Some(25.0))
      }
    }

    "return all optimizations" in {
      implicit val alg = mockAlg(IO.sleep(2.seconds) *> IO.pure((optimizedTracks, 25.0)))

      val result = for {
        optimizer <- PlaylistOptimizer.refBasedPlaylistOptimizer[IO]
        _         <- optimizer.optimize(userSessionId, playlist, parameters)
        _         <- optimizer.optimize(userSessionId, playlist, parameters)
        res       <- optimizer.getAll(userSessionId)
      } yield res

      result.asserting { optimizations =>
        optimizations.size must be(2)
      }
    }

    "delete optimization" in {
      implicit val alg = mockAlg(IO.sleep(2.seconds) *> IO.pure((optimizedTracks, 25.0)))

      val result = for {
        optimizer <- PlaylistOptimizer.refBasedPlaylistOptimizer[IO]
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
        _         <- optimizer.delete(userSessionId, id)
        res       <- optimizer.getAll(userSessionId)
      } yield res

      result.asserting(_ must be(Nil))
    }

    "return error if deleted optimization does not exist" in {
      implicit val alg = mockAlg(???)

      val result = for {
        optimizer <- PlaylistOptimizer.refBasedPlaylistOptimizer[IO]
        _         <- optimizer.delete(userSessionId, optimizationId)
        res       <- optimizer.getAll(userSessionId)
      } yield res

      result.assertThrows[OptimizationNotFound]
    }

    "return error if user id does not match" in {
      implicit val alg = mockAlg(IO.sleep(2.seconds) *> IO.pure((optimizedTracks, 25.0)))

      val result = for {
        optimizer <- PlaylistOptimizer.refBasedPlaylistOptimizer[IO]
        id        <- optimizer.optimize(userSessionId, playlist, parameters)
        res       <- optimizer.delete(UserSessionId("foo"), id)
      } yield res

      result.assertThrows[OptimizationNotFound]
    }

    def mockAlg(returnResult: => IO[(IndexedSeq[Track], Double)]) =
      new OptimizationAlgorithm[IO, Track] {
        override def optimizeSeq(items: IndexedSeq[Track], parameters: OptimizationParameters): IO[(IndexedSeq[Track], Double)] =
          returnResult
      }
  }
}

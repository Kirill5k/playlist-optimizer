package io.kirill.playlistoptimizer.core

import cats.effect.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import io.kirill.playlistoptimizer.core.common.config.AppConfig
import io.kirill.playlistoptimizer.core.health.Health
import io.kirill.playlistoptimizer.core.optimizer.{OptimizationAlgorithm, Optimizers}
import io.kirill.playlistoptimizer.free.operators.*
import io.kirill.playlistoptimizer.domain.playlist.Track
import io.kirill.playlistoptimizer.core.spotify.Spotify
import org.http4s.server.Router
import org.http4s.blaze.server.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object Application extends IOApp.Simple {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val alg = OptimizationAlgorithm.geneticAlgorithm[IO, Track](
    Crossover.bestKeySequenceTrackCrossover,
    Mutator.neighbourSwapMutator[Track],
    Evaluator.harmonicSeqBasedTracksEvaluator,
    Selector.rouletteWheelSelector[Track],
    Elitism.simple[Track]
  )

  override val run: IO[Unit] =
    Resources.make[IO].use { res =>
      for {
        config    <- AppConfig.load[IO]
        _         <- logger.info("starting playlist-optimizer app...")
        health    <- Health.make[IO]
        optimizer <- Optimizers.playlist[IO](alg)
        spotify   <- Spotify.make(res.backend, config.spotify, config.jwt)
        _ <- BlazeServerBuilder[IO]
          .withExecutionContext(runtime.compute)
          .bindHttp(config.server.port, config.server.host)
          .withHttpApp(
            Router(
              "api/spotify" -> spotify.playlistController.routesWithUserSession,
              "api"         -> optimizer.controller.routesWithUserSession,
              ""            -> health.controller.routes
            ).orNotFound
          )
          .serve
          .compile
          .drain
      } yield ()
    }
}

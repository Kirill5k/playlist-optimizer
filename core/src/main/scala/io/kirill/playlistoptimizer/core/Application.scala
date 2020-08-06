package io.kirill.playlistoptimizer.core

import cats.effect._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.kirill.playlistoptimizer.core.common.config.AppConfig
import io.kirill.playlistoptimizer.core.common.controllers.AppController
import io.kirill.playlistoptimizer.core.optimizer.Optimizer
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.optimizer.operators.{Crossover, Mutator}
import io.kirill.playlistoptimizer.core.playlist.Track
import io.kirill.playlistoptimizer.core.spotify.Spotify
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._

import scala.util.Random

object Application extends IOApp {

  val config = AppConfig.load()

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  implicit val r: Random                             = new Random()
  implicit val c: Crossover[Track]                   = Crossover.threeWaySplitCrossover
  implicit val m: Mutator[Track]                     = Mutator.randomSwapMutator[Track]
  implicit val alg: OptimizationAlgorithm[IO, Track] = OptimizationAlgorithm.geneticAlgorithm[IO, Track]

  override def run(args: List[String]): IO[ExitCode] =
    Resources.make[IO].use { res =>
      for {
        _                      <- logger.info("starting playlist-optimizer app...")
        optimizer              <- Optimizer.make[IO]
        spotify                <- Spotify.make(res.backend, config.spotify, config.jwt)
        _ <- BlazeServerBuilder[IO]
          .bindHttp(config.server.port, config.server.host)
          .withHttpApp(
            Router(
              ""            -> AppController.homeController(res.blocker).routes,
              "api"         -> optimizer.optimizationController.routes,
              "api/spotify" -> spotify.playlistController.routes
            ).orNotFound
          )
          .serve
          .compile
          .drain
        _ <- logger.info("playlist-optimizer has started")
      } yield ExitCode.Success
    }
}

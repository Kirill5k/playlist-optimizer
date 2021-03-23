package io.kirill.playlistoptimizer.core

import cats.effect._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.kirill.playlistoptimizer.core.common.config.AppConfig
import io.kirill.playlistoptimizer.core.common.controllers.{HomeController}
import io.kirill.playlistoptimizer.core.optimizer.Optimizers
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.optimizer.algorithms.operators._
import io.kirill.playlistoptimizer.core.playlist.Track
import io.kirill.playlistoptimizer.core.spotify.Spotify
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._

import scala.concurrent.ExecutionContext

object Application extends IOApp {

  val config = AppConfig.load()

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  implicit val c: Crossover[Track]                   = Crossover.bestKeySequenceTrackCrossover
  implicit val m: Mutator[Track]                     = Mutator.neighbourSwapMutator[Track]
  implicit val s: Selector[Track]                    = Selector.rouletteWheelSelector[Track]
  implicit val el: Elitism[Track]                    = Elitism.elitism[Track]
  implicit val alg: OptimizationAlgorithm[IO, Track] = OptimizationAlgorithm.geneticAlgorithm[IO, Track]

  override def run(args: List[String]): IO[ExitCode] =
    Resources.make[IO].use { res =>
      for {
        _              <- logger.info("starting playlist-optimizer app...")
        homeController <- HomeController.make(res.blocker)
        optimizer      <- Optimizers.playlist[IO]
        spotify        <- Spotify.make(res.backend, config.spotify, config.jwt)
        router = Router(
          "api/spotify" -> spotify.playlistController.routesWithUserSession,
          "api"         -> optimizer.optimizationController.routesWithUserSession,
          ""            -> homeController.routesWithUserSession
        ).orNotFound
        _ <- BlazeServerBuilder[IO](ExecutionContext.global)
          .bindHttp(config.server.port, config.server.host)
          .withHttpApp(router)
          .serve
          .compile
          .drain
      } yield ExitCode.Success
    }
}

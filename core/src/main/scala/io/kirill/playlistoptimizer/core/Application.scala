package io.kirill.playlistoptimizer.core

import cats.effect._
import cats.implicits._
import io.kirill.playlistoptimizer.core.common.config.AppConfig
import io.kirill.playlistoptimizer.core.common.controllers.AppController
import io.kirill.playlistoptimizer.core.optimizer.Optimizer
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.optimizer.operators.{Crossover, Mutator}
import io.kirill.playlistoptimizer.core.playlist.{PlaylistService, Track}
import io.kirill.playlistoptimizer.core.optimizer.operators.Crossover
import io.kirill.playlistoptimizer.core.playlist.Track
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.syntax.kleisli._
import sttp.client.{NothingT, SttpBackend}
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

object Application extends IOApp {

  val config = AppConfig.load()

  implicit val r: Random = new Random()
  implicit val c: Crossover[Track] = Crossover.bestKeySequenceTrackCrossover
  implicit val m: Mutator[Track] = Mutator.randomSwapMutator[Track]
  implicit val alg: OptimizationAlgorithm[IO, Track] = OptimizationAlgorithm.geneticAlgorithm(config.algorithms.ga)

  override def run(args: List[String]): IO[ExitCode] =
    app.use(_ => IO.never).as(ExitCode.Success)


  val app: Resource[IO, Server[IO]] =
    for {
      blocker <- Blocker[IO]
      backend <- Resource.make(AsyncHttpClientCatsBackend[IO]())(_.close())
      optimizer <- Optimizer.refBasedOptimizer[IO, Track]
      spotifyPlaylistService = PlaylistService.spotifyPlaylistService(optimizer)(config, backend)
      server <- BlazeServerBuilder[IO]
                  .withIdleTimeout(2 minutes)
                  .bindHttp(config.server.port, config.server.hostname)
                  .withHttpApp(Router(
                    "" -> AppController.homeController(blocker).routes,
                    "spotify" -> AppController.spotifyController(spotifyPlaylistService)(config).routes
                  ).orNotFound).resource
    } yield server
}

package io.kirill.playlistoptimizer

import cats.effect._
import cats.implicits._
import io.kirill.playlistoptimizer.common.configs.AppConfig
import io.kirill.playlistoptimizer.common.controllers.AppController
import io.kirill.playlistoptimizer.optimizer.Optimizer
import io.kirill.playlistoptimizer.optimizer.operators.{Crossover, Mutator}
import io.kirill.playlistoptimizer.playlist.{PlaylistService, Track}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.syntax.kleisli._
import sttp.client.{NothingT, SttpBackend}
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

object Application extends IOApp {

  implicit val r: Random = new Random()
  implicit val c: Crossover[Track] = Crossover.bestKeySequenceTrackCrossover
  implicit val m: Mutator[Track] = Mutator.randomSwapMutator[Track]

  override def run(args: List[String]): IO[ExitCode] =
    app.use(_ => IO.never).as(ExitCode.Success)

  val client: Resource[IO, SttpBackend[IO, Nothing, NothingT]] =
    Resource.make(AsyncHttpClientCatsBackend[IO]())(_.close())

  val app: Resource[IO, Server[IO]] =
    for {
      blocker <- Blocker[IO]
      backend <- client
      config <- Resource.liftF(AppConfig.load(blocker))
      optimizer = Optimizer.geneticAlgorithmOptimizer[IO, Track](200, 400, 0.3)
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

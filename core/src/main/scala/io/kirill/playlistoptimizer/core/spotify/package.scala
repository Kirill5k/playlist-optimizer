package io.kirill.playlistoptimizer.core

import cats.effect.{Concurrent, ContextShift, Resource, Sync}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.controllers.AppController
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.{Track}
import sttp.client.{NothingT, SttpBackend}

package object spotify {

  final class Spotify[F[_]](
      val playlistController: AppController[F]
  )

  object Spotify {
    def make[F[_]: Concurrent: Logger: ContextShift](
        backend: SttpBackend[F, Nothing, NothingT],
        spotifyConfig: SpotifyConfig
    )(
        implicit alg: OptimizationAlgorithm[F, Track]
    ): F[Spotify[F]] =
      for {
        service    <- SpotifyPlaylistService.make(backend, spotifyConfig)
        controller <- SpotifyPlaylistController.make(service, spotifyConfig)
      } yield new Spotify(controller)
  }
}

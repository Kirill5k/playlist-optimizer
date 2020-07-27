package io.kirill.playlistoptimizer.core

import cats.effect.{Concurrent, ContextShift, Resource, Sync}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.{PlaylistController, PlaylistOptimizer, PlaylistService, Track}
import sttp.client.{NothingT, SttpBackend}

package object spotify {

  final class Spotify[F[_]](
      val playlistController: PlaylistController[F]
  )

  object Spotify {
    def make[F[_]: Concurrent: Logger: ContextShift](
        backend: SttpBackend[F, Nothing, NothingT],
        spotifyConfig: SpotifyConfig
    )(
        implicit alg: OptimizationAlgorithm[F, Track]
    ): F[Spotify[F]] =
      for {
        optimizer  <- PlaylistOptimizer.refBasedPlaylistOptimizer[F]
        service    <- PlaylistService.spotify(optimizer, backend, spotifyConfig)
        controller <- PlaylistController.spotify(service, spotifyConfig)
      } yield new Spotify(controller)
  }
}

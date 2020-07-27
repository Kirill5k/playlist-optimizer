package io.kirill.playlistoptimizer.core

import cats.effect.{Resource, Sync}
import cats.implicits._
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.optimizer.Optimizer
import io.kirill.playlistoptimizer.core.playlist.{PlaylistController, PlaylistService, Track}
import sttp.client.{NothingT, SttpBackend}

package object spotify {

  final class Spotify[F[_]](
      val playlistController: PlaylistController[F]
  )

  object Spotify {
    def make[F[_]: Sync](
        optimizer: Optimizer[F, Track],
        backend: SttpBackend[F, Nothing, NothingT],
        spotifyConfig: SpotifyConfig
    ): F[Spotify[F]] =
      for {
        service    <- PlaylistService.spotify(optimizer, backend, spotifyConfig)
        controller <- PlaylistController.spotify(service, spotifyConfig)
      } yield new Spotify(controller)
  }
}

package io.kirill.playlistoptimizer.core

import cats.effect.{Concurrent, ContextShift, Resource, Sync}
import cats.implicits._
import io.circe.generic.auto._
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.common.config.{JwtConfig, SpotifyConfig}
import io.kirill.playlistoptimizer.core.common.controllers.AppController
import io.kirill.playlistoptimizer.core.common.jwt.JwtEncoder
import io.kirill.playlistoptimizer.core.common.json._
import io.kirill.playlistoptimizer.core.optimizer.algorithms.OptimizationAlgorithm
import io.kirill.playlistoptimizer.core.playlist.Track
import io.kirill.playlistoptimizer.core.spotify.clients.SpotifyAuthClient.SpotifyAccessToken
import sttp.client.{NothingT, SttpBackend}

package object spotify {

  final class Spotify[F[_]](
      val playlistController: AppController[F]
  )

  object Spotify {
    def make[F[_]: Concurrent: Logger: ContextShift](
        backend: SttpBackend[F, Nothing, NothingT],
        spotifyConfig: SpotifyConfig,
        jwtConfig: JwtConfig
    )(
        implicit alg: OptimizationAlgorithm[F, Track]
    ): F[Spotify[F]] =
      for {
        jwtEncoder <- JwtEncoder.circeJwtEncoder[F, SpotifyAccessToken](jwtConfig)
        service    <- SpotifyPlaylistService.make(backend, spotifyConfig)
        controller <- SpotifyPlaylistController.make(jwtEncoder, service, spotifyConfig)
      } yield new Spotify(controller)
  }
}

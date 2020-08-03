package io.kirill.playlistoptimizer.core

import java.time.Instant

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
import io.kirill.playlistoptimizer.core.spotify.clients.{SpotifyApiClient, SpotifyAuthClient}
import sttp.client.{NothingT, SttpBackend}

package object spotify {

  final case class SpotifyAccessToken(
      accessToken: String,
      refreshToken: String,
      userId: String,
      validUntil: Instant
  ) {
    def isValid: Boolean = validUntil.isAfter(Instant.now())
  }

  object SpotifyAccessToken {
    def apply(accessToken: String, refreshToken: String, userId: String, expiresIn: Int): SpotifyAccessToken =
      new SpotifyAccessToken(accessToken, refreshToken, userId, Instant.now().plusSeconds(expiresIn))
  }

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
        authClient <- SpotifyAuthClient.make(backend, spotifyConfig)
        apiClient  <- SpotifyApiClient.make(backend, spotifyConfig)
        service    <- SpotifyPlaylistService.make(authClient, apiClient)
        jwtEncoder <- JwtEncoder.circeJwtEncoder[F, SpotifyAccessToken](jwtConfig)
        controller <- SpotifyPlaylistController.make(jwtEncoder, service, spotifyConfig)
      } yield new Spotify(controller)
  }
}

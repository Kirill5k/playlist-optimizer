package io.kirill.playlistoptimizer.core.spotify

import cats.Parallel

import java.time.Instant
import cats.effect.{Concurrent, ContextShift}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.circe.generic.auto._
import io.kirill.playlistoptimizer.core.common.config.{JwtConfig, SpotifyConfig}
import io.kirill.playlistoptimizer.core.common.controllers.AppController
import io.kirill.playlistoptimizer.core.common.jwt.JwtEncoder
import io.kirill.playlistoptimizer.core.spotify.clients.{SpotifyApiClient, SpotifyAuthClient}
import sttp.client.{NothingT, SttpBackend}

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
    new SpotifyAccessToken(accessToken, refreshToken, userId, Instant.now().plusSeconds(expiresIn.toLong - 60L))
}

final class Spotify[F[_]](
    val playlistController: AppController[F]
)

object Spotify {
  def make[F[_]: Concurrent: Parallel: Logger: ContextShift](
      backend: SttpBackend[F, Nothing],
      spotifyConfig: SpotifyConfig,
      jwtConfig: JwtConfig
  ): F[Spotify[F]] =
    for {
      authClient <- SpotifyAuthClient.make(backend, spotifyConfig)
      apiClient  <- SpotifyApiClient.make(backend, spotifyConfig)
      service    <- SpotifyPlaylistService.make(authClient, apiClient)
      jwtEncoder <- JwtEncoder.circeJwtEncoder[F, SpotifyAccessToken](jwtConfig)
      controller <- SpotifyPlaylistController.make(jwtEncoder, service, spotifyConfig)
    } yield new Spotify(controller)
}

package io.kirill.playlistoptimizer.core.spotify

import cats.effect.Async
import cats.implicits._
import io.circe.generic.auto._
import io.kirill.playlistoptimizer.core.common.config.{JwtConfig, SpotifyConfig}
import io.kirill.playlistoptimizer.core.common.controllers.Controller
import io.kirill.playlistoptimizer.core.common.jwt.JwtEncoder
import io.kirill.playlistoptimizer.core.spotify.clients.{SpotifyAuthClient, SpotifyRestClient}
import org.typelevel.log4cats.Logger
import sttp.client3.SttpBackend

import java.time.Instant

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
    SpotifyAccessToken(accessToken, refreshToken, userId, Instant.now().plusSeconds(expiresIn.toLong - 60L))
}

trait Spotify[F[_]] {
  def playlistController: Controller[F]
}

object Spotify {
  def make[F[_]: Async: Logger](
      backend: SttpBackend[F, Any],
      spotifyConfig: SpotifyConfig,
      jwtConfig: JwtConfig
  ): F[Spotify[F]] =
    for {
      authClient        <- SpotifyAuthClient.make(backend, spotifyConfig)
      apiClient         <- SpotifyRestClient.make(backend, spotifyConfig)
      service           <- SpotifyPlaylistService.make(authClient, apiClient)
      jwtEncoder        <- JwtEncoder.circeJwtEncoder[F, SpotifyAccessToken](jwtConfig)
      spotifyController <- SpotifyPlaylistController.make(jwtEncoder, service, spotifyConfig)
    } yield new Spotify[F] {
      def playlistController: Controller[F] = spotifyController
    }
}

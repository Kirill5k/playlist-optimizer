package io.kirill.playlistoptimizer.core.spotify.clients

import java.time.Instant

import cats.effect.{IO, Sync}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.errors.AuthenticationRequiredError
import io.kirill.playlistoptimizer.core.spotify.clients.api.{SpotifyAuthApi, SpotifyRestApi}
import sttp.client.{NothingT, SttpBackend}

private[spotify] class SpotifyAuthClient[F[_]: Sync: Logger](
    implicit val sc: SpotifyConfig,
    val b: SttpBackend[F, Nothing, NothingT]
) {
  import SpotifyAuthClient._

  private var spotifyAccessToken: Either[Throwable, SpotifyAccessToken] = Left(
    AuthenticationRequiredError("authorization with Spotify is required")
  )

  def authorize(accessCode: String): F[Unit] =
    for {
      authResponse <- SpotifyAuthApi.authorize(accessCode)
      userResponse <- SpotifyRestApi.getCurrentUser(authResponse.access_token)
    } yield {
      spotifyAccessToken = Right(SpotifyAccessToken(authResponse.access_token, accessCode, userResponse.id, authResponse.expires_in))
      ()
    }

  def token: F[String] =
    for {
      token      <- Sync[F].fromEither(spotifyAccessToken)
      validToken <- if (token.isValid) Sync[F].pure(token) else refreshToken()
    } yield validToken.accessToken

  private def refreshToken(): F[SpotifyAccessToken] =
    for {
      token          <- Sync[F].fromEither(spotifyAccessToken)
      refreshedToken <- SpotifyAuthApi
        .refresh(token.refreshToken)
        .map(response => token.copy(accessToken = response.access_token))
    } yield {
      spotifyAccessToken = Right(refreshedToken)
      refreshedToken
    }

  def userId: F[String] =
    Sync[F].fromEither(spotifyAccessToken).map(_.userId)
}

private[spotify] object SpotifyAuthClient {
  final case class SpotifyAccessToken(accessToken: String, refreshToken: String, userId: String, validUntil: Instant) {
    def isValid: Boolean = validUntil.isAfter(Instant.now())
  }

  final object SpotifyAccessToken {
    def apply(accessToken: String, refreshToken: String, userId: String, expiresIn: Int): SpotifyAccessToken =
      new SpotifyAccessToken(accessToken, refreshToken, userId, Instant.now().plusSeconds(expiresIn))
  }
}

package io.kirill.playlistoptimizer.spotify.clients

import java.time.Instant

import cats.effect.IO
import cats.implicits._
import io.kirill.playlistoptimizer.common.configs.SpotifyConfig
import io.kirill.playlistoptimizer.common.errors.ApplicationError.UnauthorizedError
import io.kirill.playlistoptimizer.spotify.clients.api.{SpotifyAuthApi, SpotifyRestApi}
import sttp.client.{NothingT, SttpBackend}

private[spotify] class SpotifyAuthClient(implicit val sc: SpotifyConfig, val b: SttpBackend[IO, Nothing, NothingT]) {
  import SpotifyAuthClient._

  private var spotifyAccessToken: IO[SpotifyAccessToken] =
    IO.raiseError(UnauthorizedError("authorization with Spotify is required"))

  def authorize(accessCode: String): IO[Unit] = {
    spotifyAccessToken = for {
      authResponse <- SpotifyAuthApi.authorize(accessCode)
      userResponse <- SpotifyRestApi.getCurrentUser(authResponse.access_token)
    } yield SpotifyAccessToken(authResponse.access_token, authResponse.refresh_token, userResponse.id, authResponse.expires_in)
    spotifyAccessToken *> IO.pure(())
  }

  def token: IO[String] = {
    spotifyAccessToken = for {
      token <- spotifyAccessToken
      validToken <- if (token.isValid) IO.pure(token)
                    else SpotifyAuthApi.refresh(token.refreshToken).map(refreshedToken => token.copy(accessToken = refreshedToken.access_token))
    } yield validToken
    spotifyAccessToken.map(_.accessToken)
  }

  def userId: IO[String] = spotifyAccessToken.map(_.userId)
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

package io.kirill.playlistoptimizer.spotify.clients

import java.time.Instant

import cats.effect.IO
import cats.implicits._
import io.kirill.playlistoptimizer.common.configs.SpotifyConfig
import io.kirill.playlistoptimizer.common.errors.ApplicationError.AuthenticationRequiredError
import io.kirill.playlistoptimizer.spotify.clients.api.{SpotifyAuthApi, SpotifyRestApi}
import sttp.client.{NothingT, SttpBackend}

private[spotify] class SpotifyAuthClient(implicit val sc: SpotifyConfig, val b: SttpBackend[IO, Nothing, NothingT]) {
  import SpotifyAuthClient._

  private var spotifyAccessToken: Either[Throwable, SpotifyAccessToken] = Left(AuthenticationRequiredError("authorization with Spotify is required"))

  def authorize(accessCode: String): IO[Unit] =
    for {
      authResponse <- SpotifyAuthApi.authorize(accessCode)
      userResponse <- SpotifyRestApi.getCurrentUser(authResponse.access_token)
    } yield {
      spotifyAccessToken = Right(SpotifyAccessToken(authResponse.access_token, accessCode, userResponse.id, authResponse.expires_in))
      ()
    }

  def token: IO[String] = {
    for {
      token <- IO.fromEither(spotifyAccessToken)
      validToken <- if (token.isValid) IO.pure(token) else refreshToken()
    } yield validToken.accessToken
  }

  private def refreshToken(): IO[SpotifyAccessToken] = {
    for {
      token <- IO.fromEither(spotifyAccessToken)
      refreshedToken <- SpotifyAuthApi.refresh(token.refreshToken).map(response => token.copy(accessToken = response.access_token))
    } yield  {
      spotifyAccessToken = Right(refreshedToken)
      refreshedToken
    }
  }

  def userId: IO[String] = IO.fromEither(spotifyAccessToken).map(_.userId)
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

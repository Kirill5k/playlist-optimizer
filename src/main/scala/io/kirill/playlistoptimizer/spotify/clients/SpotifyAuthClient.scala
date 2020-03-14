package io.kirill.playlistoptimizer.spotify.clients

import java.time.Instant

import cats.effect.IO
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import io.kirill.playlistoptimizer.spotify.clients.api.{SpotifyAuthApi, SpotifyRestApi}
import sttp.client.{NothingT, SttpBackend}

private[spotify] class SpotifyAuthClient(accessCode: String)(implicit val sc: SpotifyConfig, val b: SttpBackend[IO, Nothing, NothingT]) {
  import SpotifyAuthClient._

  private var spotifyAccessToken: IO[SpotifyAccessToken] = for {
    authResponse <- SpotifyAuthApi.authorize(accessCode)
    userResponse <- SpotifyRestApi.getCurrentUser(authResponse.access_token)
  } yield SpotifyAccessToken(authResponse.access_token, authResponse.refresh_token, userResponse.id, authResponse.expires_in)

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

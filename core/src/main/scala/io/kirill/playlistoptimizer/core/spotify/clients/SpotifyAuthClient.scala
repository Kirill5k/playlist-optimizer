package io.kirill.playlistoptimizer.core.spotify.clients

import cats.effect.Sync
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.spotify.SpotifyAccessToken
import io.kirill.playlistoptimizer.core.spotify.clients.api.{SpotifyAuthApi, SpotifyRestApi}
import sttp.client3.SttpBackend

private[spotify] class SpotifyAuthClient[F[_]: Sync: Logger](implicit
    private val sc: SpotifyConfig,
    private val b: SttpBackend[F, Any]
) {

  def authorize(accessCode: String): F[SpotifyAccessToken] =
    for {
      authResponse <- SpotifyAuthApi.authorize(accessCode)
      userResponse <- SpotifyRestApi.getCurrentUser(authResponse.access_token)
    } yield SpotifyAccessToken(
      authResponse.access_token,
      authResponse.refresh_token,
      userResponse.id,
      authResponse.expires_in
    )

  def refresh(accessToken: SpotifyAccessToken): F[SpotifyAccessToken] =
    SpotifyAuthApi
      .refresh(accessToken.refreshToken)
      .map(res => SpotifyAccessToken(res.access_token, accessToken.refreshToken, accessToken.userId, res.expires_in))
}

private[spotify] object SpotifyAuthClient {

  def make[F[_]: Sync: Logger](
      backend: SttpBackend[F, Any],
      spotifyConfig: SpotifyConfig
  ): F[SpotifyAuthClient[F]] = {
    implicit val b  = backend
    implicit val sc = spotifyConfig
    Sync[F].delay(new SpotifyAuthClient[F]())
  }
}

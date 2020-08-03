package io.kirill.playlistoptimizer.core.spotify.clients.api

import cats.implicits._
import cats.MonadError
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._
import SpotifyError.SpotifyAuthError
import SpotifyResponse.{SpotifyAuthRefreshResponse, SpotifyAuthResponse}
import cats.effect.Sync
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.spotify.SpotifyPlaylistController
import io.kirill.playlistoptimizer.core.spotify.clients.api.SpotifyError.SpotifyAuthError
import io.kirill.playlistoptimizer.core.spotify.clients.api.SpotifyResponse.{SpotifyAuthRefreshResponse, SpotifyAuthResponse}
import sttp.client._
import sttp.client.circe._
import sttp.model.MediaType

private[spotify] object SpotifyAuthApi {

  def authorize[F[_]: Logger: Sync](code: String)(
      implicit sc: SpotifyConfig,
      b: SttpBackend[F, Nothing, NothingT]
  ): F[SpotifyAuthResponse] =
    Logger[F].info("sending authorization request to spotify") *>
      getToken[F, SpotifyAuthResponse](Map("grant_type" -> "authorization_code", "code" -> code, "redirect_uri" -> sc.redirectUrl))

  def refresh[F[_]: Logger: Sync](refreshToken: String)(
      implicit sc: SpotifyConfig,
      b: SttpBackend[F, Nothing, NothingT]
  ): F[SpotifyAuthRefreshResponse] =
    Logger[F].info("sending token refresh request to spotify") *>
      getToken[F, SpotifyAuthRefreshResponse](Map("refresh_token" -> refreshToken))

  private def getToken[F[_]: Logger: Sync, R <: SpotifyResponse: Decoder](requestBody: Map[String, String])(
      implicit sc: SpotifyConfig,
      b: SttpBackend[F, Nothing, NothingT]
  ): F[R] =
    Logger[F].info("send get token request to spotify") *>
      basicRequest
        .body(requestBody)
        .auth
        .basic(sc.clientId, sc.clientSecret)
        .contentType(MediaType.ApplicationXWwwFormUrlencoded)
        .post(uri"${sc.authUrl}/api/token")
        .response(asJson[R])
        .send()
        .flatMap(r => mapResponseBody[F, R](r.body))

  private def mapResponseBody[F[_]: Logger: Sync, R <: SpotifyResponse](
      responseBody: Either[ResponseError[io.circe.Error], R]
  ): F[R] =
    responseBody match {
      case Right(success) =>
        Sync[F].pure(success)
      case Left(error) =>
        Logger[F].error(s"error sending auth request to spotify: ${error.body}") *>
          Sync[F].fromEither(decode[SpotifyAuthError](error.body).flatMap(Left(_)))
    }
}

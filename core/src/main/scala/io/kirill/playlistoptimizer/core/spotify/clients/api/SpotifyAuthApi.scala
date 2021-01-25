package io.kirill.playlistoptimizer.core.spotify.clients.api

import cats.effect.Sync
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.errors.SpotifyApiError
import io.kirill.playlistoptimizer.core.spotify.clients.api.SpotifyError.SpotifyAuthError
import io.kirill.playlistoptimizer.core.spotify.clients.api.SpotifyResponse.{SpotifyAuthRefreshResponse, SpotifyAuthResponse}
import sttp.client3._
import sttp.client3.circe._
import sttp.model.MediaType

private[spotify] object SpotifyAuthApi {

  def authorize[F[_]: Logger: Sync](code: String)(
      implicit sc: SpotifyConfig,
      b: SttpBackend[F, Nothing]
  ): F[SpotifyAuthResponse] =
    Logger[F].info("sending authorization request to spotify") *>
      getToken[F, SpotifyAuthResponse](Map("grant_type" -> "authorization_code", "code" -> code, "redirect_uri" -> sc.redirectUrl))

  def refresh[F[_]: Logger: Sync](refreshToken: String)(
      implicit sc: SpotifyConfig,
      b: SttpBackend[F, Nothing]
  ): F[SpotifyAuthRefreshResponse] =
    Logger[F].info("sending token refresh request to spotify") *>
      getToken[F, SpotifyAuthRefreshResponse](Map("refresh_token" -> refreshToken, "grant_type" -> "refresh_token"))

  private def getToken[F[_]: Logger: Sync, R <: SpotifyResponse: Decoder](requestBody: Map[String, String])(
      implicit sc: SpotifyConfig,
      b: SttpBackend[F, Nothing]
  ): F[R] =
    Logger[F].info("send get token request to spotify") *>
      basicRequest
        .body(requestBody)
        .auth
        .basic(sc.clientId, sc.clientSecret)
        .contentType(MediaType.ApplicationXWwwFormUrlencoded)
        .post(uri"${sc.authUrl}/api/token")
        .response(asJsonEither[SpotifyAuthError, R])
        .send(b)
        .flatMap(r => mapResponseBody[F, R](r.body))

  private def mapResponseBody[F[_]: Logger: Sync, R <: SpotifyResponse](
      responseBody: Either[ResponseException[SpotifyAuthError, io.circe.Error], R]
  ): F[R] =
    responseBody match {
      case Right(success) => success.pure[F]
      case Left(DeserializationException(body, error)) =>
        Logger[F].error(s"error deserializing spotify response: ${error.getMessage}\n$body") *>
          SpotifyApiError(s"error deserializing spotify response: ${error.getMessage}").raiseError[F, R]
      case Left(HttpError(spotifyError, code)) =>
        Logger[F].error(s"http error sending auth request to spotify: $code - ${spotifyError.error_description}") *>
          SpotifyApiError(spotifyError.error_description).raiseError[F, R]
      case Left(error) =>
        Logger[F].error(s"internal error sending auth request to spotify: ${error.getMessage}") *>
          SpotifyApiError(error.getMessage).raiseError[F, R]
    }
}

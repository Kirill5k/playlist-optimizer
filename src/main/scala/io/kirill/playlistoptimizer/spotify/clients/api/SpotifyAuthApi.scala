package io.kirill.playlistoptimizer.spotify.clients.api

import cats.implicits._
import cats.MonadError
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._
import io.kirill.playlistoptimizer.spotify.clients.api.SpotifyError.SpotifyAuthError
import io.kirill.playlistoptimizer.spotify.clients.api.SpotifyResponse.{SpotifyAuthResponse, SpotifyAuthRefreshResponse}
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import sttp.client._
import sttp.client.circe._
import sttp.model.MediaType

object SpotifyAuthApi {

  def authorize[F[_]](code: String)(
    implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyAuthResponse] =
    getToken[F, SpotifyAuthResponse](Map("grant_type" -> "authorization_code", "code" -> code, "redirect_uri" -> C.auth.redirectUri))

  def refresh[F[_]](refreshToken: String)(
    implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyAuthRefreshResponse] =
    getToken[F, SpotifyAuthRefreshResponse](Map("refresh_token" -> refreshToken))

  private def getToken[F[_], R <: SpotifyResponse: Decoder](requestBody: Map[String, String])(
    implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[R] =
    basicRequest
      .body(requestBody)
      .auth.basic(C.auth.clientId, C.auth.clientSecret)
      .contentType(MediaType.ApplicationXWwwFormUrlencoded)
      .post(uri"${C.auth.baseUrl}${C.auth.tokenPath}")
      .response(asJson[R])
      .send()
      .flatMap(r => mapResponseBody[F, R](r.body))


  private def mapResponseBody[F[_], R <: SpotifyResponse](responseBody: Either[ResponseError[io.circe.Error], R])(
    implicit m: MonadError[F, Throwable]
  ): F[R] =
    responseBody match {
      case Right(success) => m.pure(success)
      case Left(error) => m.fromEither(decode[SpotifyAuthError](error.body).flatMap(Left(_)))
    }
}
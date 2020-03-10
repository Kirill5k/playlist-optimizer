package io.kirill.playlistoptimizer.clients.spotify

import cats.implicits._
import cats.MonadError
import io.circe.generic.auto._
import io.circe.parser._
import io.kirill.playlistoptimizer.clients.spotify.SpotifyError.SpotifyAuthError
import io.kirill.playlistoptimizer.clients.spotify.SpotifyResponse.SpotifyAuthResponse
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import sttp.client._
import sttp.client.circe._
import sttp.model.MediaType

object SpotifyAuthApi {

  def authorize[F[_]](code: String)(
    implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyAuthResponse] =
    basicRequest
      .body(Map("grant_type" -> "authorization_code", "code" -> code, "redirect_uri" -> C.auth.redirectUri))
      .auth.basic(C.auth.clientId, C.auth.clientSecret)
      .contentType(MediaType.ApplicationXWwwFormUrlencoded)
      .post(uri"${C.auth.baseUrl}${C.auth.tokenPath}")
      .response(asJson[SpotifyAuthResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyAuthResponse](r.body))

  private def mapResponseBody[F[_], R <: SpotifyResponse](responseBody: Either[ResponseError[io.circe.Error], R])(
    implicit m: MonadError[F, Throwable]
  ): F[R] =
    responseBody match {
      case Right(success) => m.pure(success)
      case Left(error) => m.fromEither(decode[SpotifyAuthError](error.body).flatMap(Left(_)))
    }
}

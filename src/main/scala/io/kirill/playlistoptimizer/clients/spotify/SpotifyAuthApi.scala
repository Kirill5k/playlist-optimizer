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

  private val authUserRequest = Map(
    "scope" -> "playlist-read-private playlist-modify-public playlist-modify-private"
  )

  private val authClientRequesy = Map(
    "grant_type" -> "client_credentials"
  )

  def authenticateClient[F[_]](
    implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyAuthResponse] =
    basicRequest
      .body(authClientRequesy)
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

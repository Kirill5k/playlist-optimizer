package io.kirill.playlistoptimizer.clients.spotify

import cats.implicits._
import cats.MonadError
import io.circe.generic.auto._
import io.circe.parser._
import io.kirill.playlistoptimizer.clients.spotify.SpotifyAuthResponse.{SpotifyAuthErrorResponse, SpotifyAuthSuccessResponse}
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import io.kirill.playlistoptimizer.domain.ApiClientError._
import sttp.client._
import sttp.client.circe._
import sttp.model.{MediaType, StatusCode}

object SpotifyApi {

  private val authRequestBody = Map("grant_type" -> "client_credentials")

  def authenticate[F[_]](config: SpotifyConfig)(implicit B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]): F[SpotifyAuthSuccessResponse] = {
    basicRequest
      .body(authRequestBody)
      .auth.basic(config.clientId, config.clientSecret)
      .contentType(MediaType.ApplicationXWwwFormUrlencoded)
      .post(uri"${config.baseUrl}${config.authPath}")
      .response(asJson[SpotifyAuthSuccessResponse])
      .send()
      .flatMap { res =>
        res.body match {
          case Right(success) => M.pure(success)
          case Left(error) => M.fromEither(decode[SpotifyAuthErrorResponse](error.body).flatMap(e => Left(AuthError(s"error authenticating with spotify: ${e.error_description}"))))
        }
      }
  }
}

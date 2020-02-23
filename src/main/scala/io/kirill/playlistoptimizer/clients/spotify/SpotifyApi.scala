package io.kirill.playlistoptimizer.clients.spotify

import cats.implicits._
import cats.MonadError
import io.circe.generic.auto._
import io.circe.parser._
import io.kirill.playlistoptimizer.clients.spotify.SpotifyError.SpotifyAuthError
import io.kirill.playlistoptimizer.clients.spotify.SpotifyResponse.SpotifyAuthResponse
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import io.kirill.playlistoptimizer.domain.ApiClientError._
import sttp.client._
import sttp.client.circe._
import sttp.model.{MediaType, StatusCode}

object SpotifyApi {

  private val authRequestBody = Map("grant_type" -> "client_credentials")

  def authenticate[F[_]](implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]): F[SpotifyAuthResponse] = {
    basicRequest
      .body(authRequestBody)
      .auth.basic(C.clientId, C.clientSecret)
      .contentType(MediaType.ApplicationXWwwFormUrlencoded)
      .post(uri"${C.baseUrl}${C.authPath}")
      .response(asJson[SpotifyAuthResponse])
      .send()
      .flatMap { res =>
        res.body match {
          case Right(success) => M.pure(success)
          case Left(error) => M.fromEither(decode[SpotifyAuthError](error.body).flatMap(Left(_)))
        }
      }
  }
}

package io.kirill.playlistoptimizer.spotify.clients.api

import cats.implicits._
import cats.MonadError
import com.typesafe.scalalogging.Logger
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._
import io.kirill.playlistoptimizer.spotify.clients.api.SpotifyError.SpotifyAuthError
import io.kirill.playlistoptimizer.spotify.clients.api.SpotifyResponse.{SpotifyAuthRefreshResponse, SpotifyAuthResponse}
import io.kirill.playlistoptimizer.common.configs.SpotifyConfig
import io.kirill.playlistoptimizer.spotify.SpotifyPlaylistController
import sttp.client._
import sttp.client.circe._
import sttp.model.MediaType

object SpotifyAuthApi {
  private val logger = Logger("SpotifyAuthApi")

  def authorize[F[_]](code: String)(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyAuthResponse] = {
    logger.info("sending authorization request to spotify")
    getToken[F, SpotifyAuthResponse](Map("grant_type" -> "authorization_code", "code" -> code, "redirect_uri" -> sc.redirectUri))
  }

  def refresh[F[_]](refreshToken: String)(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyAuthRefreshResponse] = {
    logger.info("sending token refresh request to spotify")
    getToken[F, SpotifyAuthRefreshResponse](Map("refresh_token" -> refreshToken))
  }

  private def getToken[F[_], R <: SpotifyResponse: Decoder](requestBody: Map[String, String])(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[R] =
    basicRequest
      .body(requestBody)
      .auth.basic(sc.clientId, sc.clientSecret)
      .contentType(MediaType.ApplicationXWwwFormUrlencoded)
      .post(uri"${sc.authUrl}/api/token")
      .response(asJson[R])
      .send()
      .flatMap(r => mapResponseBody[F, R](r.body))

  private def mapResponseBody[F[_], R <: SpotifyResponse](responseBody: Either[ResponseError[io.circe.Error], R])(
    implicit m: MonadError[F, Throwable]
  ): F[R] =
    responseBody match {
      case Right(success) => m.pure(success)
      case Left(error) =>
        logger.error(s"error sending auth request to spotify: ${error.body}")
        m.fromEither(decode[SpotifyAuthError](error.body).flatMap(Left(_)))
    }
}

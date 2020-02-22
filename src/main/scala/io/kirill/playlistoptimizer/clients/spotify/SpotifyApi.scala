package io.kirill.playlistoptimizer.clients.spotify

import cats.implicits._
import cats.MonadError
import io.circe.generic.auto._
import io.circe.parser._
import io.kirill.playlistoptimizer.clients.spotify.SpotifyAuthResponse.{AuthError, AuthSuccess}
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import sttp.client._
import sttp.client.circe._
import sttp.model.{MediaType, StatusCode}

object SpotifyApi {

  private val authRequestBody = Map("grant_type" -> "client_credentials")

  def authenticate[F[_]: MonadError](config: SpotifyConfig)(implicit B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]): F[String] = {
    basicRequest
      .body(authRequestBody)
      .auth.basic(config.clientId, config.clientSecret)
      .contentType(MediaType.ApplicationXWwwFormUrlencoded)
      .post(uri"${config.baseUrl}${config.authPath}")
      .response(fromMetadata(meta => if (meta.code == StatusCode.Ok) asJson[AuthSuccess] else asJson[AuthError]))
      .send()
      .flatMap(_.body.fold(M.raiseError, M.pure))
  }
}

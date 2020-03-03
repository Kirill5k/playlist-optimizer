package io.kirill.playlistoptimizer.clients.spotify

import cats.implicits._
import cats.MonadError
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._
import io.kirill.playlistoptimizer.clients.spotify.SpotifyError._
import io.kirill.playlistoptimizer.clients.spotify.SpotifyResponse._
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import sttp.client._
import sttp.client.circe._
import sttp.model.MediaType

object SpotifyApi {

  private val authRequestBody = Map("grant_type" -> "client_credentials")

  def authenticate[F[_]]
    (implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]): F[SpotifyAuthResponse] =
    basicRequest
      .body(authRequestBody)
      .auth.basic(C.auth.clientId, C.auth.clientSecret)
      .contentType(MediaType.ApplicationXWwwFormUrlencoded)
      .post(uri"${C.auth.baseUrl}${C.auth.tokenPath}")
      .response(asJson[SpotifyAuthResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyAuthResponse, SpotifyAuthError](r.body))

  def getAudioAnalysis[F[_]]
    (authToken: String, trackId: String)
    (implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]): F[SpotifyAudioAnalysisResponse] =
    basicRequest
      .auth.bearer(authToken)
      .get(uri"${C.api.baseUrl}${C.api.audioAnalysisPath}/$trackId")
      .response(asJson[SpotifyAudioAnalysisResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyAudioAnalysisResponse, SpotifyRegularError](r.body))

  def getPlaylist[F[_]]
    (authToken: String, playlistId: String)
    (implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]): F[SpotifyPlaylistResponse] =
    basicRequest
      .auth.bearer(authToken)
      .get(uri"${C.api.baseUrl}${C.api.playlistsPath}/$playlistId")
      .response(asJson[SpotifyPlaylistResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyPlaylistResponse, SpotifyRegularError](r.body))

  def getUserPlaylists[F[_]]
    (authToken: String, userId: String)
    (implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]): F[SpotifyPlaylistsResponse] =
    basicRequest
      .auth.bearer(authToken)
      .get(uri"${C.api.baseUrl}${C.api.usersPath}/$userId/playlists")
      .response(asJson[SpotifyPlaylistsResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyPlaylistsResponse, SpotifyRegularError](r.body))

  private def mapResponseBody[F[_], R <: SpotifyResponse, E <: Throwable : Decoder]
    (responseBody: Either[ResponseError[io.circe.Error], R])
    (implicit M: MonadError[F, Throwable]): F[R] =
    responseBody match {
      case Right(success) => M.pure(success)
      case Left(error) => M.fromEither(decode[E](error.body).flatMap(Left(_)))
    }
}

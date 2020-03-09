package io.kirill.playlistoptimizer.clients.spotify

import cats.implicits._
import cats.MonadError
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._
import io.kirill.playlistoptimizer.clients.spotify.SpotifyError._
import io.kirill.playlistoptimizer.clients.spotify.SpotifyRequest._
import io.kirill.playlistoptimizer.clients.spotify.SpotifyResponse._
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import sttp.client._
import sttp.client.circe._
import sttp.model.MediaType

object SpotifyApi {

  private val userAuthRequestBody = Map(
    "scope" -> "playlist-read-private playlist-modify-public playlist-modify-private"
  )

  private val clientAuthRequestBody = Map(
    "grant_type" -> "client_credentials"
  )

  def authenticateClient[F[_]](
    implicit c: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], m: MonadError[F, Throwable]
  ): F[SpotifyAuthResponse] =
    basicRequest
      .body(clientAuthRequestBody)
      .auth.basic(c.auth.clientId, c.auth.clientSecret)
      .contentType(MediaType.ApplicationXWwwFormUrlencoded)
      .post(uri"${c.auth.baseUrl}${c.auth.tokenPath}")
      .response(asJson[SpotifyAuthResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyAuthResponse, SpotifyAuthError](r.body))

  def getAudioAnalysis[F[_]](authToken: String, trackId: String)(
    implicit c: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], m: MonadError[F, Throwable]
  ): F[SpotifyAudioAnalysisResponse] =
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${c.api.baseUrl}${c.api.audioAnalysisPath}/$trackId")
      .response(asJson[SpotifyAudioAnalysisResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyAudioAnalysisResponse, SpotifyRegularError](r.body))

  def getAudioFeatures[F[_]](authToken: String, trackId: String)(
    implicit c: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], m: MonadError[F, Throwable]
  ): F[SpotifyAudioFeaturesResponse] =
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${c.api.baseUrl}${c.api.audioFeaturesPath}/$trackId")
      .response(asJson[SpotifyAudioFeaturesResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyAudioFeaturesResponse, SpotifyRegularError](r.body))

  def getPlaylist[F[_]](authToken: String, playlistId: String)(
    implicit c: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], m: MonadError[F, Throwable]
  ): F[SpotifyPlaylistResponse] =
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${c.api.baseUrl}${c.api.playlistsPath}/$playlistId")
      .response(asJson[SpotifyPlaylistResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyPlaylistResponse, SpotifyRegularError](r.body))

  def getUserPlaylists[F[_]](authToken: String, userId: String)(
    implicit c: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], m: MonadError[F, Throwable]
  ): F[SpotifyPlaylistsResponse] =
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${c.api.baseUrl}${c.api.usersPath}/$userId/playlists")
      .response(asJson[SpotifyPlaylistsResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyPlaylistsResponse, SpotifyRegularError](r.body))

  def createPlaylist[F[_]](authToken: String, userId: String, playlistName: String, playlistDescription: Option[String])(
    implicit c: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], m: MonadError[F, Throwable]
  ): F[SpotifyPlaylistResponse] =
    basicRequest
      .body(CreatePlaylistRequest(playlistName, playlistDescription))
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .post(uri"${c.api.baseUrl}${c.api.usersPath}/$userId/playlists")
      .response(asJson[SpotifyPlaylistResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyPlaylistResponse, SpotifyRegularError](r.body))

  def addTracksToPlaylist[F[_]](authToken: String, playlistId: String, uris: Seq[String], position: Option[Int] = None)(
    implicit c: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], m: MonadError[F, Throwable]
  ): F[SpotifyOperationSuccessResponse] =
    basicRequest
      .body(AddTracksToPlaylistRequest(uris, position))
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .post(uri"${c.api.baseUrl}${c.api.playlistsPath}/$playlistId/tracks")
      .response(asJson[SpotifyOperationSuccessResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyOperationSuccessResponse, SpotifyRegularError](r.body))

  def replaceTracksInPlaylist[F[_]](authToken: String, playlistId: String, uris: Seq[String])(
    implicit c: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], m: MonadError[F, Throwable]
  ): F[Unit] =
    basicRequest
      .body(ReplaceTracksInPlaylistRequest(uris))
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .put(uri"${c.api.baseUrl}${c.api.playlistsPath}/$playlistId/tracks")
      .send()
      .flatMap { r =>
        r.body match {
          case Right(_) => m.pure(())
          case Left(error) => m.fromEither(decode[SpotifyRegularError](error).flatMap(Left(_)))
        }
      }

  private def mapResponseBody[F[_], R <: SpotifyResponse, E <: Throwable : Decoder](responseBody: Either[ResponseError[io.circe.Error], R])(
    implicit m: MonadError[F, Throwable]
  ): F[R] =
    responseBody match {
      case Right(success) => m.pure(success)
      case Left(error) => m.fromEither(decode[E](error.body).flatMap(Left(_)))
    }
}

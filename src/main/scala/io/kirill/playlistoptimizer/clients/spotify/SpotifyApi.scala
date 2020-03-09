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
      .flatMap(r => mapResponseBody[F, SpotifyAuthResponse, SpotifyAuthError](r.body))

  def getAudioAnalysis[F[_]](authToken: String, trackId: String)(
    implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyAudioAnalysisResponse] =
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${C.api.baseUrl}${C.api.audioAnalysisPath}/$trackId")
      .response(asJson[SpotifyAudioAnalysisResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyAudioAnalysisResponse, SpotifyRegularError](r.body))

  def getAudioFeatures[F[_]](authToken: String, trackId: String)(
    implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyAudioFeaturesResponse] =
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${C.api.baseUrl}${C.api.audioFeaturesPath}/$trackId")
      .response(asJson[SpotifyAudioFeaturesResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyAudioFeaturesResponse, SpotifyRegularError](r.body))

  def getPlaylist[F[_]](authToken: String, playlistId: String)(
    implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyPlaylistResponse] =
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${C.api.baseUrl}${C.api.playlistsPath}/$playlistId")
      .response(asJson[SpotifyPlaylistResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyPlaylistResponse, SpotifyRegularError](r.body))

  def getUserPlaylists[F[_]](authToken: String, userId: String)(
    implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyPlaylistsResponse] =
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${C.api.baseUrl}${C.api.usersPath}/$userId/playlists")
      .response(asJson[SpotifyPlaylistsResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyPlaylistsResponse, SpotifyRegularError](r.body))

  def createPlaylist[F[_]](authToken: String, userId: String, playlistName: String, playlistDescription: Option[String])(
    implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyPlaylistResponse] =
    basicRequest
      .body(CreatePlaylistRequest(playlistName, playlistDescription))
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .post(uri"${C.api.baseUrl}${C.api.usersPath}/$userId/playlists")
      .response(asJson[SpotifyPlaylistResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyPlaylistResponse, SpotifyRegularError](r.body))

  def addTracksToPlaylist[F[_]](authToken: String, playlistId: String, uris: Seq[String], position: Option[Int] = None)(
    implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyOperationSuccessResponse] =
    basicRequest
      .body(AddTracksToPlaylistRequest(uris, position))
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .post(uri"${C.api.baseUrl}${C.api.playlistsPath}/$playlistId/tracks")
      .response(asJson[SpotifyOperationSuccessResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyOperationSuccessResponse, SpotifyRegularError](r.body))

  def replaceTracksInPlaylist[F[_]](authToken: String, playlistId: String, uris: Seq[String])(
    implicit C: SpotifyConfig, B: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[Unit] =
    basicRequest
      .body(ReplaceTracksInPlaylistRequest(uris))
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .put(uri"${C.api.baseUrl}${C.api.playlistsPath}/$playlistId/tracks")
      .send()
      .flatMap { r =>
        r.body match {
          case Right(_) => M.pure(())
          case Left(error) => M.fromEither(decode[SpotifyRegularError](error).flatMap(Left(_)))
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

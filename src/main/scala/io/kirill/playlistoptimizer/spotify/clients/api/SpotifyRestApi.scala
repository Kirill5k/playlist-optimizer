package io.kirill.playlistoptimizer.spotify.clients.api

import cats.implicits._
import cats.MonadError
import io.circe.generic.auto._
import io.circe.parser._
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import io.kirill.playlistoptimizer.spotify.clients.api.SpotifyResponse._
import io.kirill.playlistoptimizer.spotify.clients.api.SpotifyRequest._
import io.kirill.playlistoptimizer.spotify.clients.api.SpotifyError._
import sttp.client._
import sttp.client.circe._
import sttp.model.MediaType

object SpotifyRestApi {

  def getCurrentUser[F[_]](authToken: String)(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyUserResponse] =
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${sc.api.baseUrl}${sc.api.currentUserPath}")
      .response(asJson[SpotifyUserResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyUserResponse](r.body))

  def getAudioAnalysis[F[_]](authToken: String, trackId: String)(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyAudioAnalysisResponse] =
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${sc.api.baseUrl}${sc.api.audioAnalysisPath}/$trackId")
      .response(asJson[SpotifyAudioAnalysisResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyAudioAnalysisResponse](r.body))

  def getAudioFeatures[F[_]](authToken: String, trackId: String)(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyAudioFeaturesResponse] =
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${sc.api.baseUrl}${sc.api.audioFeaturesPath}/$trackId")
      .response(asJson[SpotifyAudioFeaturesResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyAudioFeaturesResponse](r.body))

  def getPlaylist[F[_]](authToken: String, playlistId: String)(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyPlaylistResponse] =
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${sc.api.baseUrl}${sc.api.playlistsPath}/$playlistId")
      .response(asJson[SpotifyPlaylistResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyPlaylistResponse](r.body))

  def getUserPlaylists[F[_]](authToken: String)(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyPlaylistsResponse] =
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${sc.api.baseUrl}${sc.api.currentUserPath}/playlists")
      .response(asJson[SpotifyPlaylistsResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyPlaylistsResponse](r.body))

  def createPlaylist[F[_]](authToken: String, userId: String, playlistName: String, playlistDescription: Option[String])(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyPlaylistResponse] =
    basicRequest
      .body(CreatePlaylistRequest(playlistName, playlistDescription))
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .post(uri"${sc.api.baseUrl}${sc.api.usersPath}/$userId/playlists")
      .response(asJson[SpotifyPlaylistResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyPlaylistResponse](r.body))

  def addTracksToPlaylist[F[_]](authToken: String, playlistId: String, uris: Seq[String], position: Option[Int] = None)(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyOperationSuccessResponse] =
    basicRequest
      .body(AddTracksToPlaylistRequest(uris, position))
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .post(uri"${sc.api.baseUrl}${sc.api.playlistsPath}/$playlistId/tracks")
      .response(asJson[SpotifyOperationSuccessResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyOperationSuccessResponse](r.body))

  def replaceTracksInPlaylist[F[_]](authToken: String, playlistId: String, uris: Seq[String])(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[Unit] =
    basicRequest
      .body(ReplaceTracksInPlaylistRequest(uris))
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .put(uri"${sc.api.baseUrl}${sc.api.playlistsPath}/$playlistId/tracks")
      .send()
      .flatMap { r =>
        r.body match {
          case Right(_) => M.pure(())
          case Left(error) => M.fromEither(decode[SpotifyRegularError](error).flatMap(Left(_)))
        }
      }

  private def mapResponseBody[F[_], R <: SpotifyResponse](responseBody: Either[ResponseError[io.circe.Error], R])(
    implicit m: MonadError[F, Throwable]
  ): F[R] =
    responseBody match {
      case Right(success) => m.pure(success)
      case Left(error) => m.fromEither(decode[SpotifyRegularError](error.body).flatMap(Left(_)))
    }
}

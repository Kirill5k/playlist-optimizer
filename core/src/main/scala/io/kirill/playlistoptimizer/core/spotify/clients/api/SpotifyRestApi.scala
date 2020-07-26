package io.kirill.playlistoptimizer.core.spotify.clients.api

import cats.implicits._
import cats.MonadError
import com.typesafe.scalalogging.Logger
import io.circe.generic.auto._
import io.circe.parser._
import SpotifyAuthApi.logger
import SpotifyResponse._
import SpotifyRequest._
import SpotifyError._
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import sttp.client._
import sttp.client.circe._
import sttp.model.MediaType

object SpotifyRestApi {
  private val logger = Logger("SpotifyRestApi")

  def getCurrentUser[F[_]](authToken: String)(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyUserResponse] = {
    logger.info("sending get current user request")
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${sc.restUrl}/v1/me")
      .response(asJson[SpotifyUserResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyUserResponse](r.body))
  }

  def getAudioAnalysis[F[_]](authToken: String, trackId: String)(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyAudioAnalysisResponse] = {
    logger.info(s"sending get audio analysis from track $trackId request")
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${sc.restUrl}/v1/audio-analysis/$trackId")
      .response(asJson[SpotifyAudioAnalysisResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyAudioAnalysisResponse](r.body))
  }

  def getAudioFeatures[F[_]](authToken: String, trackId: String)(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyAudioFeaturesResponse] = {
    logger.info(s"sending get audio features from track $trackId request")
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${sc.restUrl}/v1/audio-features/$trackId")
      .response(asJson[SpotifyAudioFeaturesResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyAudioFeaturesResponse](r.body))
  }

  def getPlaylist[F[_]](authToken: String, playlistId: String)(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyPlaylistResponse] = {
    logger.info(s"sending get playlist $playlistId request")
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${sc.restUrl}/v1/playlists/$playlistId")
      .response(asJson[SpotifyPlaylistResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyPlaylistResponse](r.body))
  }

  def getUserPlaylists[F[_]](authToken: String)(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyPlaylistsResponse] = {
    logger.info(s"sending get current user playlists request")
    basicRequest
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${sc.restUrl}/v1/me/playlists")
      .response(asJson[SpotifyPlaylistsResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyPlaylistsResponse](r.body))
  }

  def createPlaylist[F[_]](authToken: String, userId: String, playlistName: String, playlistDescription: Option[String])(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyPlaylistResponse] = {
    logger.info(s"sending create new playlist request $playlistName")
    basicRequest
      .body(CreatePlaylistRequest(playlistName, playlistDescription))
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .post(uri"${sc.restUrl}/v1/users/$userId/playlists")
      .response(asJson[SpotifyPlaylistResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyPlaylistResponse](r.body))
  }

  def addTracksToPlaylist[F[_]](authToken: String, playlistId: String, uris: Seq[String], position: Option[Int] = None)(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[SpotifyOperationSuccessResponse] = {
    logger.info(s"sending add tracks to playlist $playlistId request")
    basicRequest
      .body(AddTracksToPlaylistRequest(uris, position))
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .post(uri"${sc.restUrl}/v1/playlists/$playlistId/tracks")
      .response(asJson[SpotifyOperationSuccessResponse])
      .send()
      .flatMap(r => mapResponseBody[F, SpotifyOperationSuccessResponse](r.body))
  }

  def replaceTracksInPlaylist[F[_]](authToken: String, playlistId: String, uris: Seq[String])(
    implicit sc: SpotifyConfig, b: SttpBackend[F, Nothing, NothingT], M: MonadError[F, Throwable]
  ): F[Unit] = {
    logger.info(s"sending replace tracks in playlist $playlistId request")
    basicRequest
      .body(ReplaceTracksInPlaylistRequest(uris))
      .auth.bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .put(uri"${sc.restUrl}/v1/playlists/$playlistId/tracks")
      .send()
      .flatMap { r =>
        r.body match {
          case Right(_) => M.pure(())
          case Left(error) => M.fromEither(decode[SpotifyRegularError](error).flatMap(Left(_)))
        }
      }
  }

  private def mapResponseBody[F[_], R <: SpotifyResponse](responseBody: Either[ResponseError[io.circe.Error], R])(
    implicit m: MonadError[F, Throwable]
  ): F[R] =
    responseBody match {
      case Right(success) => m.pure(success)
      case Left(error) =>
        logger.error(s"error sending rest request to spotify: ${error.body}")
        m.fromEither(decode[SpotifyRegularError](error.body).flatMap(Left(_)))
    }
}
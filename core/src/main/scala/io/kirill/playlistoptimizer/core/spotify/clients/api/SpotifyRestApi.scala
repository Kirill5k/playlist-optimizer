package io.kirill.playlistoptimizer.core.spotify.clients.api

import cats.implicits._
import io.circe.generic.auto._
import SpotifyResponse._
import SpotifyRequest._
import SpotifyError._
import cats.effect.Sync
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.errors.SpotifyApiError
import sttp.client3._
import sttp.client3.circe._
import sttp.model.MediaType

object SpotifyRestApi {

  def findTrack[F[_]: Logger: Sync](authToken: String, query: String, limit: Int = 1)(implicit
      sc: SpotifyConfig,
      b: SttpBackend[F, Any]
  ): F[SpotifySearchResponse] =
    basicRequest.auth
      .bearer(authToken)
      .contentType(MediaType.ApplicationJson)
      .get(uri"${sc.restUrl}/v1/search?q=$query&type=track&limit=$limit")
      .response(asJsonEither[SpotifyRegularError, SpotifySearchResponse])
      .send(b)
      .flatMap(r => mapResponseBody[F, SpotifySearchResponse](r.body))

  def getCurrentUser[F[_]: Logger: Sync](authToken: String)(implicit
      sc: SpotifyConfig,
      b: SttpBackend[F, Any]
  ): F[SpotifyUserResponse] =
    Logger[F].info("sending get current user request") *>
      basicRequest.auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .get(uri"${sc.restUrl}/v1/me")
        .response(asJsonEither[SpotifyRegularError, SpotifyUserResponse])
        .send(b)
        .flatMap(r => mapResponseBody[F, SpotifyUserResponse](r.body))

  def getAudioAnalysis[F[_]: Logger: Sync](authToken: String, trackId: String)(implicit
      sc: SpotifyConfig,
      b: SttpBackend[F, Any]
  ): F[SpotifyAudioAnalysisResponse] =
    Logger[F].info(s"sending get audio analysis from track $trackId request") *>
      basicRequest.auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .get(uri"${sc.restUrl}/v1/audio-analysis/$trackId")
        .response(asJsonEither[SpotifyRegularError, SpotifyAudioAnalysisResponse])
        .send(b)
        .flatMap(r => mapResponseBody[F, SpotifyAudioAnalysisResponse](r.body))

  def getAudioFeatures[F[_]: Logger: Sync](authToken: String, trackId: String)(implicit
      sc: SpotifyConfig,
      b: SttpBackend[F, Any]
  ): F[SpotifyAudioFeaturesResponse] =
    Logger[F].info(s"sending get audio features from track $trackId request") *>
      basicRequest.auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .get(uri"${sc.restUrl}/v1/audio-features/$trackId")
        .response(asJsonEither[SpotifyRegularError, SpotifyAudioFeaturesResponse])
        .send(b)
        .flatMap(r => mapResponseBody[F, SpotifyAudioFeaturesResponse](r.body))

  def getMultipleAudioFeatures[F[_]: Logger: Sync](authToken: String, trackIds: List[String])(implicit
      sc: SpotifyConfig,
      b: SttpBackend[F, Any]
  ): F[SpotifyMultipleAudioFeaturesResponse] =
    Logger[F].info(s"sending get audio features from tracks $trackIds request") *>
      basicRequest.auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .get(uri"${sc.restUrl}/v1/audio-features?ids=${trackIds.mkString(",")}")
        .response(asJsonEither[SpotifyRegularError, SpotifyMultipleAudioFeaturesResponse])
        .send(b)
        .flatMap(r => mapResponseBody[F, SpotifyMultipleAudioFeaturesResponse](r.body))

  def getPlaylist[F[_]: Logger: Sync](authToken: String, playlistId: String)(implicit
      sc: SpotifyConfig,
      b: SttpBackend[F, Any]
  ): F[SpotifyPlaylistResponse] =
    Logger[F].info(s"sending get playlist $playlistId request") *>
      basicRequest.auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .get(uri"${sc.restUrl}/v1/playlists/$playlistId")
        .response(asJsonEither[SpotifyRegularError, SpotifyPlaylistResponse])
        .send(b)
        .flatMap(r => mapResponseBody[F, SpotifyPlaylistResponse](r.body))

  def getUserPlaylists[F[_]: Logger: Sync](authToken: String)(implicit
      sc: SpotifyConfig,
      b: SttpBackend[F, Any]
  ): F[SpotifyPlaylistsResponse] =
    Logger[F].info(s"sending get current user playlists request") *>
      basicRequest.auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .get(uri"${sc.restUrl}/v1/me/playlists")
        .response(asJsonEither[SpotifyRegularError, SpotifyPlaylistsResponse])
        .send(b)
        .flatMap(r => mapResponseBody[F, SpotifyPlaylistsResponse](r.body))

  def createPlaylist[F[_]: Logger: Sync](
      authToken: String,
      userId: String,
      playlistName: String,
      playlistDescription: Option[String]
  )(implicit
      sc: SpotifyConfig,
      b: SttpBackend[F, Any]
  ): F[SpotifyPlaylistResponse] =
    Logger[F].info(s"sending create new playlist request $playlistName") *>
      basicRequest
        .body(CreatePlaylistRequest(playlistName, playlistDescription))
        .auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .post(uri"${sc.restUrl}/v1/users/$userId/playlists")
        .response(asJsonEither[SpotifyRegularError, SpotifyPlaylistResponse])
        .send(b)
        .flatMap(r => mapResponseBody[F, SpotifyPlaylistResponse](r.body))

  def addTracksToPlaylist[F[_]: Logger: Sync](
      authToken: String,
      playlistId: String,
      uris: Seq[String],
      position: Option[Int] = None
  )(implicit
      sc: SpotifyConfig,
      b: SttpBackend[F, Any]
  ): F[SpotifyOperationSuccessResponse] =
    Logger[F].info(s"sending add tracks to playlist $playlistId request") *>
      basicRequest
        .body(AddTracksToPlaylistRequest(uris, position))
        .auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .post(uri"${sc.restUrl}/v1/playlists/$playlistId/tracks")
        .response(asJsonEither[SpotifyRegularError, SpotifyOperationSuccessResponse])
        .send(b)
        .flatMap(r => mapResponseBody[F, SpotifyOperationSuccessResponse](r.body))

  def replaceTracksInPlaylist[F[_]: Logger: Sync](authToken: String, playlistId: String, uris: Seq[String])(implicit
      sc: SpotifyConfig,
      b: SttpBackend[F, Any]
  ): F[Unit] =
    Logger[F].info(s"sending replace tracks in playlist $playlistId request") *>
      basicRequest
        .body(ReplaceTracksInPlaylistRequest(uris))
        .auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .put(uri"${sc.restUrl}/v1/playlists/$playlistId/tracks")
        .response(asJsonEither[SpotifyRegularError, SpotifyOperationSuccessResponse])
        .send(b)
        .flatMap(r => mapResponseBody[F, SpotifyOperationSuccessResponse](r.body))
        .void

  private def mapResponseBody[F[_]: Logger: Sync, R <: SpotifyResponse](
      responseBody: Either[ResponseException[SpotifyRegularError, io.circe.Error], R]
  ): F[R] =
    responseBody match {
      case Right(success) => success.pure[F]
      case Left(DeserializationException(body, error)) =>
        Logger[F].error(s"error deserializing spotify response: ${error.getMessage}\n$body") *>
          SpotifyApiError(s"error deserializing spotify response: ${error.getMessage}").raiseError[F, R]
      case Left(HttpError(spotifyError, code)) =>
        Logger[F].error(s"http error sending rest api request to spotify: $code - ${spotifyError.error.message}") *>
          SpotifyApiError(spotifyError.error.message).raiseError[F, R]
      case Left(error) =>
        Logger[F].error(s"internal error sending rest api request to spotify: ${error.getMessage}") *>
          SpotifyApiError(error.getMessage).raiseError[F, R]
    }
}

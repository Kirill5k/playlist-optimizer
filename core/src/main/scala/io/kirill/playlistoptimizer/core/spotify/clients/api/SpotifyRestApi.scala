package io.kirill.playlistoptimizer.core.spotify.clients.api

import cats.implicits._
import io.circe.generic.auto._
import io.circe.parser._
import SpotifyResponse._
import SpotifyRequest._
import SpotifyError._
import cats.effect.Sync
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.errors.SpotifyApiError
import sttp.client._
import sttp.client.circe._
import sttp.model.MediaType

object SpotifyRestApi {

  def getCurrentUser[F[_]: Logger: Sync](authToken: String)(
      implicit sc: SpotifyConfig,
      b: SttpBackend[F, Nothing, NothingT]
  ): F[SpotifyUserResponse] =
    Logger[F].info("sending get current user request") *>
      basicRequest.auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .get(uri"${sc.restUrl}/v1/me")
        .response(asJson[SpotifyUserResponse])
        .send()
        .flatMap(r => mapResponseBody[F, SpotifyUserResponse](r.body))

  def getAudioAnalysis[F[_]: Logger: Sync](authToken: String, trackId: String)(
      implicit sc: SpotifyConfig,
      b: SttpBackend[F, Nothing, NothingT]
  ): F[SpotifyAudioAnalysisResponse] =
    Logger[F].info(s"sending get audio analysis from track $trackId request") *>
      basicRequest.auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .get(uri"${sc.restUrl}/v1/audio-analysis/$trackId")
        .response(asJson[SpotifyAudioAnalysisResponse])
        .send()
        .flatMap(r => mapResponseBody[F, SpotifyAudioAnalysisResponse](r.body))

  def getAudioFeatures[F[_]: Logger: Sync](authToken: String, trackId: String)(
      implicit sc: SpotifyConfig,
      b: SttpBackend[F, Nothing, NothingT]
  ): F[SpotifyAudioFeaturesResponse] =
    Logger[F].info(s"sending get audio features from track $trackId request") *>
      basicRequest.auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .get(uri"${sc.restUrl}/v1/audio-features/$trackId")
        .response(asJson[SpotifyAudioFeaturesResponse])
        .send()
        .flatMap(r => mapResponseBody[F, SpotifyAudioFeaturesResponse](r.body))

  def getMultipleAudioFeatures[F[_]: Logger: Sync](authToken: String, trackIds: List[String])(
    implicit sc: SpotifyConfig,
    b: SttpBackend[F, Nothing, NothingT]
  ): F[SpotifyMultipleAudioFeaturesResponse] =
    Logger[F].info(s"sending get audio features from tracks $trackIds request") *>
      basicRequest.auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .get(uri"${sc.restUrl}/v1/audio-features?ids=$trackIds")
        .response(asJson[SpotifyMultipleAudioFeaturesResponse])
        .send()
        .flatMap(r => mapResponseBody[F, SpotifyMultipleAudioFeaturesResponse](r.body))

  def getPlaylist[F[_]: Logger: Sync](authToken: String, playlistId: String)(
      implicit sc: SpotifyConfig,
      b: SttpBackend[F, Nothing, NothingT]
  ): F[SpotifyPlaylistResponse] =
    Logger[F].info(s"sending get playlist $playlistId request") *>
      basicRequest.auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .get(uri"${sc.restUrl}/v1/playlists/$playlistId")
        .response(asJson[SpotifyPlaylistResponse])
        .send()
        .flatMap(r => mapResponseBody[F, SpotifyPlaylistResponse](r.body))

  def getUserPlaylists[F[_]: Logger: Sync](authToken: String)(
      implicit sc: SpotifyConfig,
      b: SttpBackend[F, Nothing, NothingT]
  ): F[SpotifyPlaylistsResponse] =
    Logger[F].info(s"sending get current user playlists request") *>
      basicRequest.auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .get(uri"${sc.restUrl}/v1/me/playlists")
        .response(asJson[SpotifyPlaylistsResponse])
        .send()
        .flatMap(r => mapResponseBody[F, SpotifyPlaylistsResponse](r.body))

  def createPlaylist[F[_]: Logger: Sync](
      authToken: String,
      userId: String,
      playlistName: String,
      playlistDescription: Option[String]
  )(
      implicit sc: SpotifyConfig,
      b: SttpBackend[F, Nothing, NothingT]
  ): F[SpotifyPlaylistResponse] =
    Logger[F].info(s"sending create new playlist request $playlistName") *>
      basicRequest
        .body(CreatePlaylistRequest(playlistName, playlistDescription))
        .auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .post(uri"${sc.restUrl}/v1/users/$userId/playlists")
        .response(asJson[SpotifyPlaylistResponse])
        .send()
        .flatMap(r => mapResponseBody[F, SpotifyPlaylistResponse](r.body))

  def addTracksToPlaylist[F[_]: Logger: Sync](
      authToken: String,
      playlistId: String,
      uris: Seq[String],
      position: Option[Int] = None
  )(
      implicit sc: SpotifyConfig,
      b: SttpBackend[F, Nothing, NothingT]
  ): F[SpotifyOperationSuccessResponse] =
    Logger[F].info(s"sending add tracks to playlist $playlistId request") *>
      basicRequest
        .body(AddTracksToPlaylistRequest(uris, position))
        .auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .post(uri"${sc.restUrl}/v1/playlists/$playlistId/tracks")
        .response(asJson[SpotifyOperationSuccessResponse])
        .send()
        .flatMap(r => mapResponseBody[F, SpotifyOperationSuccessResponse](r.body))

  def replaceTracksInPlaylist[F[_]: Logger: Sync](authToken: String, playlistId: String, uris: Seq[String])(
      implicit sc: SpotifyConfig,
      b: SttpBackend[F, Nothing, NothingT]
  ): F[Unit] =
    Logger[F].info(s"sending replace tracks in playlist $playlistId request") *>
      basicRequest
        .body(ReplaceTracksInPlaylistRequest(uris))
        .auth
        .bearer(authToken)
        .contentType(MediaType.ApplicationJson)
        .put(uri"${sc.restUrl}/v1/playlists/$playlistId/tracks")
        .send()
        .flatMap { r =>
          r.body match {
            case Right(_)    => Sync[F].unit
            case Left(error) => Sync[F].fromEither(decode[SpotifyRegularError](error).flatMap(Left(_)))
          }
        }

  private def mapResponseBody[F[_]: Logger: Sync, R <: SpotifyResponse](
      responseBody: Either[ResponseError[io.circe.Error], R]
  ): F[R] =
    responseBody match {
      case Right(success) =>
        Sync[F].pure(success)
      case Left(error) =>
        Logger[F].error(s"error sending rest request to spotify: ${error.body}") *>
          Sync[F].fromEither(decode[SpotifyRegularError](error.body).map(e => SpotifyApiError(e.error.message)).flatMap(Left(_)))
    }
}

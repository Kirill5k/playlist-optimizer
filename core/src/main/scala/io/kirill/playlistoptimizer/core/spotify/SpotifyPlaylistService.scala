package io.kirill.playlistoptimizer.core.spotify

import cats.Monad
import cats.effect.Concurrent
import cats.implicits._
import io.kirill.playlistoptimizer.core.playlist.{Playlist, Track}
import io.kirill.playlistoptimizer.core.spotify.clients.{SpotifyAuthClient, SpotifyRestClient}
import fs2.Stream

trait SpotifyPlaylistService[F[_]] {
  def save(accessToken: SpotifyAccessToken, playlist: Playlist): F[SpotifyAccessToken]
  def authenticate(accessCode: String): F[SpotifyAccessToken]
  def getAll(accessToken: SpotifyAccessToken): F[(Seq[Playlist], SpotifyAccessToken)]
  def findByName(accessToken: SpotifyAccessToken, name: String): F[(Playlist, SpotifyAccessToken)]
  def findTracksByNames(accessToken: SpotifyAccessToken, names: List[String]): F[(List[Track], SpotifyAccessToken)]
  def findTrackByName(accessToken: SpotifyAccessToken, name: String): F[(Track, SpotifyAccessToken)]
}

private final class LiveSpotifyPlaylistService[F[_]: Concurrent](
    private val authClient: SpotifyAuthClient[F],
    private val apiClient: SpotifyRestClient[F]
) extends SpotifyPlaylistService[F] {

  def authenticate(accessCode: String): F[SpotifyAccessToken] =
    authClient.authorize(accessCode)

  def getAll(accessToken: SpotifyAccessToken): F[(Seq[Playlist], SpotifyAccessToken)] =
    for {
      token     <- if (accessToken.isValid) accessToken.pure[F] else authClient.refresh(accessToken)
      playlists <- apiClient.getAllPlaylists(token.accessToken)
    } yield (playlists, token)

  def findByName(accessToken: SpotifyAccessToken, name: String): F[(Playlist, SpotifyAccessToken)] =
    for {
      token    <- if (accessToken.isValid) accessToken.pure[F] else authClient.refresh(accessToken)
      playlist <- apiClient.findPlaylistByName(token.accessToken, name)
    } yield (playlist, token)

  def save(accessToken: SpotifyAccessToken, playlist: Playlist): F[SpotifyAccessToken] =
    for {
      token <- if (accessToken.isValid) accessToken.pure[F] else authClient.refresh(accessToken)
      _     <- apiClient.createPlaylist(token.accessToken, token.userId, playlist)
    } yield token

  def findTracksByNames(accessToken: SpotifyAccessToken, names: List[String]): F[(List[Track], SpotifyAccessToken)] =
    for {
      token <- if (accessToken.isValid) accessToken.pure[F] else authClient.refresh(accessToken)
      tracks <- Stream
        .emits(names)
        .covary[F]
        .parEvalMap(Int.MaxValue)(name => apiClient.findTrackByName(token.accessToken, name))
        .compile
        .toList
    } yield (tracks, token)

  def findTrackByName(accessToken: SpotifyAccessToken, name: String): F[(Track, SpotifyAccessToken)] =
    for {
      token    <- if (accessToken.isValid) accessToken.pure[F] else authClient.refresh(accessToken)
      playlist <- apiClient.findTrackByName(token.accessToken, name)
    } yield (playlist, token)
}

object SpotifyPlaylistService {
  def make[F[_]: Concurrent](
      authClient: SpotifyAuthClient[F],
      apiClient: SpotifyRestClient[F]
  ): F[SpotifyPlaylistService[F]] =
    Monad[F].pure(new LiveSpotifyPlaylistService(authClient, apiClient))
}

package io.kirill.playlistoptimizer.core.spotify

import cats.effect.Sync
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.playlist.{Playlist}
import io.kirill.playlistoptimizer.core.spotify.clients.SpotifyAuthClient.SpotifyAccessToken
import io.kirill.playlistoptimizer.core.spotify.clients.{SpotifyApiClient, SpotifyAuthClient}
import sttp.client.{NothingT, SttpBackend}

class SpotifyPlaylistService[F[_]: Sync: Logger](
    implicit sc: SpotifyConfig,
    b: SttpBackend[F, Nothing, NothingT]
) {

  private val authClient = new SpotifyAuthClient[F]()
  private val apiClient  = new SpotifyApiClient[F]()

  def authenticate(accessCode: String): F[SpotifyAccessToken] =
    authClient.authorize(accessCode)

  def getAll: F[Seq[Playlist]] =
    for {
      token     <- authClient.token
      playlists <- apiClient.getAllPlaylists(token)
    } yield playlists

  def findByName(name: String): F[Playlist] =
    for {
      token    <- authClient.token
      playlist <- apiClient.findPlaylistByName(token, name)
    } yield playlist

  def save(playlist: Playlist): F[Unit] =
    for {
      userId <- authClient.userId
      token  <- authClient.token
      _      <- apiClient.createPlaylist(token, userId, playlist)
    } yield ()
}

object SpotifyPlaylistService {
  def make[F[_]: Sync: Logger](
      backend: SttpBackend[F, Nothing, NothingT],
      spotifyConfig: SpotifyConfig
  ): F[SpotifyPlaylistService[F]] = {
    implicit val sc: SpotifyConfig                    = spotifyConfig
    implicit val b: SttpBackend[F, Nothing, NothingT] = backend
    Sync[F].delay(new SpotifyPlaylistService())
  }
}

package io.kirill.playlistoptimizer.core.spotify

import cats.effect.Sync
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.playlist.{Playlist, PlaylistOptimizer, PlaylistService}
import io.kirill.playlistoptimizer.core.spotify.clients.{SpotifyApiClient, SpotifyAuthClient}
import sttp.client.{NothingT, SttpBackend}

class SpotifyPlaylistService[F[_]: Sync: Logger](
    override val optimizer: PlaylistOptimizer[F]
)(
    implicit sc: SpotifyConfig,
    b: SttpBackend[F, Nothing, NothingT]
) extends PlaylistService[F] {

  private val authClient = new SpotifyAuthClient[F]()
  private val apiClient  = new SpotifyApiClient[F]()

  def authenticate(accessCode: String): F[Unit] =
    authClient.authorize(accessCode)

  override def getAll: F[Seq[Playlist]] =
    for {
      token     <- authClient.token
      playlists <- apiClient.getAllPlaylists(token)
    } yield playlists

  override def findByName(name: String): F[Playlist] =
    for {
      token    <- authClient.token
      playlist <- apiClient.findPlaylistByName(token, name)
    } yield playlist

  override def save(playlist: Playlist): F[Unit] =
    for {
      userId <- authClient.userId
      token  <- authClient.token
      _      <- apiClient.createPlaylist(token, userId, playlist)
    } yield ()
}

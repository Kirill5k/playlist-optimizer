package io.kirill.playlistoptimizer.core.spotify

import cats.effect.Sync
import cats.implicits._
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.optimizer.Optimizer
import io.kirill.playlistoptimizer.core.playlist.{Playlist, PlaylistService, Track}
import io.kirill.playlistoptimizer.core.spotify.clients.{SpotifyApiClient, SpotifyAuthClient}
import io.kirill.playlistoptimizer.core.playlist.Playlist
import io.kirill.playlistoptimizer.core.spotify.clients.SpotifyApiClient
import sttp.client.{NothingT, SttpBackend}

import scala.util.Random

class SpotifyPlaylistService[F[_]: Sync](
    override val optimizer: Optimizer[F, Track]
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

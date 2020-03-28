package io.kirill.playlistoptimizer.spotify

import cats.effect._
import io.kirill.playlistoptimizer.common.configs.SpotifyConfig
import io.kirill.playlistoptimizer.optimizer.Optimizer
import io.kirill.playlistoptimizer.optimizer.operators.{Crossover, Mutator}
import io.kirill.playlistoptimizer.playlist.{Playlist, PlaylistService, Track}
import io.kirill.playlistoptimizer.spotify.clients.{SpotifyApiClient, SpotifyAuthClient}
import sttp.client.{NothingT, SttpBackend}

import scala.util.Random

class SpotifyPlaylistService(override val optimizer: Optimizer[IO, Track])(implicit sc: SpotifyConfig, b: SttpBackend[IO, Nothing, NothingT]) extends PlaylistService[IO] {
  override protected implicit val r: Random = new Random()

  private val authClient: SpotifyAuthClient = new SpotifyAuthClient()
  private val apiClient: SpotifyApiClient = new SpotifyApiClient()

  def authenticate(accessCode: String): IO[Unit] =
    authClient.authorize(accessCode)

  override def getAll: IO[Seq[Playlist]] =
    for {
      token <- authClient.token
      playlists <- apiClient.getAllPlaylists(token)
    } yield playlists

  override def findByName(name: String): IO[Playlist] =
    for {
      token <- authClient.token
      playlist <- apiClient.findPlaylistByName(token, name)
    } yield playlist

  override def save(playlist: Playlist): IO[Unit] =
    for {
      userId <- authClient.userId
      token <- authClient.token
      _ <- apiClient.createPlaylist(token, userId, playlist)
    } yield ()
}

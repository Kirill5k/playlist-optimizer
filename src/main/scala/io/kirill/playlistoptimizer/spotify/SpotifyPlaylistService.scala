package io.kirill.playlistoptimizer.spotify

import cats.effect._
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import io.kirill.playlistoptimizer.optimizer.Optimizer
import io.kirill.playlistoptimizer.optimizer.operators.{Crossover, Mutator}
import io.kirill.playlistoptimizer.playlist.{Playlist, PlaylistService, Track}
import io.kirill.playlistoptimizer.spotify.clients.{SpotifyApiClient, SpotifyAuthClient}
import sttp.client.{NothingT, SttpBackend}

import scala.util.Random

class SpotifyPlaylistService(accessCode: String)(implicit val C: SpotifyConfig, val B: SttpBackend[IO, Nothing, NothingT]) extends PlaylistService[IO] {
  override protected implicit val r: Random = new Random()

  implicit val c: Crossover[Track] = Crossover.bestKeySequenceTrackCrossover
  implicit val m: Mutator[Track] = Mutator.randomSwapMutator[Track]

  override protected def optimizer: Optimizer[IO, Track] = Optimizer.geneticAlgorithmOptimizer(250, 500, 0.3)

  private val authClient = new SpotifyAuthClient(accessCode)
  private val apiClient = new SpotifyApiClient()

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

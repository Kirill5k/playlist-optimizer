package io.kirill.playlistoptimizer.spotify

import cats.effect._
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import io.kirill.playlistoptimizer.optimizer.Optimizer
import io.kirill.playlistoptimizer.playlist.{Playlist, PlaylistService, Track}
import sttp.client.{NothingT, SttpBackend}

import scala.util.Random

class SpotifyPlaylistService(accessCode: String)(implicit val C: SpotifyConfig, val B: SttpBackend[IO, Nothing, NothingT]) extends PlaylistService[IO] {
  override protected implicit val r: Random = new Random()

  override protected def optimizer: Optimizer[Track] = Optimizer.geneticAlgorithmOptimizer(250, 500, 0.3)

  override def getAll: IO[Seq[Playlist]] = ???
  override def findByName(name: String): IO[Playlist] = ???
  override def save(playlist: Playlist): IO[Playlist] = ???
}

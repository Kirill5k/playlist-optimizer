package io.kirill.playlistoptimizer.core.spotify.clients

import cats.Monad
import cats.effect.{Sync, Async}
import cats.implicits.*
import fs2.Stream
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.errors.{SpotifyPlaylistNotFound, SpotifyTrackNotFound}
import io.kirill.playlistoptimizer.core.playlist.{Playlist, PlaylistSource, Track}
import io.kirill.playlistoptimizer.core.spotify.clients.api.responses.{PlaylistTrack, SpotifyAudioFeaturesResponse}
import io.kirill.playlistoptimizer.core.spotify.clients.api.{SpotifyMapper, SpotifyRestApi}
import org.typelevel.log4cats.Logger
import sttp.client3.SttpBackend

trait SpotifyRestClient[F[_]] {
  def createPlaylist(token: String, userId: String, playlist: Playlist): F[Unit]
  def getAllPlaylists(token: String): F[List[Playlist]]
  def findPlaylistByName(token: String, name: String): F[Playlist]
  def findTrackByName(token: String, name: String): F[Track]
}

final private[spotify] class LiveSpotifyRestClient[F[_]: Async: Logger](implicit
    private val sc: SpotifyConfig,
    private val b: SttpBackend[F, Any]
) extends SpotifyRestClient[F] {

  def createPlaylist(token: String, userId: String, playlist: Playlist): F[Unit] =
    SpotifyRestApi
      .createPlaylist(token, userId, playlist.name, playlist.description)
      .map(_.id)
      .flatMap { pid =>
        val trackUris = playlist.tracks.map(_.source.uri)
        SpotifyRestApi.addTracksToPlaylist(token, pid, trackUris)
      }
      .void

  def getAllPlaylists(token: String): F[List[Playlist]] =
    Stream
      .evalSeq(SpotifyRestApi.getUserPlaylists(token).map(_.items))
      .parEvalMap(Int.MaxValue)(pl => findById(token, pl.id))
      .compile
      .toList

  def findPlaylistByName(token: String, name: String): F[Playlist] =
    SpotifyRestApi
      .getUserPlaylists(token)
      .map(_.items.find(_.name.equalsIgnoreCase(name)))
      .flatMap(pl => Sync[F].fromOption(pl.map(_.id), SpotifyPlaylistNotFound(name)))
      .flatMap(id => findById(token, id))

  private def findById(token: String, id: String): F[Playlist] =
    for {
      playlist <- SpotifyRestApi.getPlaylist(token, id)
      playListTracks = playlist.tracks.items.map(_.track)
      tracksDetails <- getTrackDetails(token, playListTracks)
      tracks = tracksDetails.map(details => SpotifyMapper.toDomain(details._1, details._2)).toVector
    } yield Playlist(playlist.name, playlist.description, tracks, PlaylistSource.Spotify)

  private def getTrackDetails(
      token: String,
      tracks: Seq[PlaylistTrack]
  ): F[List[(PlaylistTrack, SpotifyAudioFeaturesResponse)]] = {
    val trackIds = tracks.map(_.id).distinct.toList
    SpotifyRestApi
      .getMultipleAudioFeatures(token, trackIds)
      .map(_.audio_features.groupBy(_.id))
      .map { featuresByIds =>
        tracks.map(t => (t, featuresByIds(t.id).head)).toList
      }
  }

  def findTrackByName(token: String, name: String): F[Track] =
    for {
      searchResult <- SpotifyRestApi.findTrack(token, SpotifyMapper.sanitiseTrackSearchQuery(name))
      track        <- Sync[F].fromOption(searchResult.tracks.items.headOption, SpotifyTrackNotFound(name))
      features     <- SpotifyRestApi.getAudioFeatures(token, track.id)
    } yield SpotifyMapper.toDomain(track, features)
}

private[spotify] object SpotifyRestClient {

  def make[F[_]: Async: Logger](
      backend: SttpBackend[F, Any],
      spotifyConfig: SpotifyConfig
  ): F[SpotifyRestClient[F]] = {
    implicit val b  = backend
    implicit val sc = spotifyConfig
    Monad[F].pure(new LiveSpotifyRestClient[F]())
  }
}

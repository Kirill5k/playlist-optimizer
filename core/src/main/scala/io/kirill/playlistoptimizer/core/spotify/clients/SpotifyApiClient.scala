package io.kirill.playlistoptimizer.core.spotify.clients

import cats.Parallel
import cats.effect.Sync
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.common.errors.{SpotifyPlaylistNotFound, SpotifyTrackNotFound}
import io.kirill.playlistoptimizer.core.playlist.{Playlist, PlaylistSource, Track}
import io.kirill.playlistoptimizer.core.spotify.clients.api.SpotifyResponse.{PlaylistTrack, SpotifyAudioFeaturesResponse}
import io.kirill.playlistoptimizer.core.spotify.clients.api.{SpotifyMapper, SpotifyRestApi}
import sttp.client.{NothingT, SttpBackend}

private[spotify] class SpotifyApiClient[F[_]: Sync: Parallel: Logger](implicit
    val sc: SpotifyConfig,
    val b: SttpBackend[F, Nothing, NothingT]
) {

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
    SpotifyRestApi
      .getUserPlaylists(token)
      .flatMap(_.items.parTraverse(p => findById(token, p.id)))

  def findPlaylistByName(token: String, name: String): F[Playlist] =
    getPlaylistId(token, name)
      .flatMap(id => findById(token, id))

  private def findById(token: String, id: String): F[Playlist] =
    for {
      playlist <- SpotifyRestApi.getPlaylist(token, id)
      playListTracks = playlist.tracks.items.map(_.track)
      tracksDetails <- getTrackDetails(token, playListTracks)
      tracks = tracksDetails.map(details => SpotifyMapper.toDomain(details._1, details._2)).toVector
    } yield Playlist(playlist.name, playlist.description, tracks, PlaylistSource.Spotify)

  private def getPlaylistId(token: String, name: String): F[String] =
    SpotifyRestApi
      .getUserPlaylists(token)
      .map(_.items.find(_.name.equalsIgnoreCase(name)))
      .flatMap(pl => Sync[F].fromOption(pl, SpotifyPlaylistNotFound(name)))
      .map(_.id)

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
      searchResult <- SpotifyRestApi.findTrack(token, name)
      track        <- Sync[F].fromOption(searchResult.tracks.items.headOption, SpotifyTrackNotFound(name))
      features     <- SpotifyRestApi.getAudioFeatures(token, track.id)
    } yield SpotifyMapper.toDomain(track, features)
}

private[spotify] object SpotifyApiClient {

  def make[F[_]: Sync: Parallel: Logger](
      backend: SttpBackend[F, Nothing, NothingT],
      spotifyConfig: SpotifyConfig
  ): F[SpotifyApiClient[F]] = {
    implicit val b  = backend
    implicit val sc = spotifyConfig
    Sync[F].delay(new SpotifyApiClient[F]())
  }
}

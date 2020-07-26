package io.kirill.playlistoptimizer.core.spotify.clients

import cats.effect.IO
import fs2.Stream
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import io.kirill.playlistoptimizer.core.playlist.{Playlist, PlaylistSource}
import io.kirill.playlistoptimizer.core.spotify.clients.api.{SpotifyMapper, SpotifyRestApi}
import io.kirill.playlistoptimizer.core.playlist.Playlist
import io.kirill.playlistoptimizer.core.spotify.clients.api.SpotifyResponse.{PlaylistTrack, SpotifyAudioFeaturesResponse}
import io.kirill.playlistoptimizer.core.spotify.clients.api.SpotifyMapper
import sttp.client.{NothingT, SttpBackend}

private[spotify] class SpotifyApiClient(implicit val sc: SpotifyConfig, val b: SttpBackend[IO, Nothing, NothingT]) {

  def createPlaylist(token: String, userId: String, playlist: Playlist): IO[Unit] =
    for {
      newPlaylist <- SpotifyRestApi.createPlaylist(token, userId, playlist.name, playlist.description)
      trackUris = playlist.tracks.map(_.source.uri)
      _ <- SpotifyRestApi.addTracksToPlaylist(token, newPlaylist.id, trackUris)
    } yield ()

  def getAllPlaylists(token: String): IO[Seq[Playlist]] =
    for {
      userPlaylists <- SpotifyRestApi.getUserPlaylists(token)
      playlists <- Stream.emits(userPlaylists.items).evalMap(p => findById(token, p.id)).compile.toList
    } yield playlists


  def findPlaylistByName(token: String, name: String): IO[Playlist] = {
    for {
      id <- getPlaylistId(token, name)
      playlist <- findById(token, id)
    } yield playlist
  }

  private def findById(token: String, id: String): IO[Playlist] = {
    for {
      playlist <- SpotifyRestApi.getPlaylist(token, id)
      playListTracks = playlist.tracks.items.map(_.track)
      tracksDetails <- getTrackDetails(token, playListTracks)
      tracks = tracksDetails.map(details => SpotifyMapper.toDomain(details._1, details._2))
    } yield Playlist(playlist.name, playlist.description, tracks, PlaylistSource.Spotify)
  }

  private def getPlaylistId(token: String, name: String): IO[String] =
    SpotifyRestApi.getUserPlaylists(token)
      .map(_.items.find(_.name.equalsIgnoreCase(name)))
      .map(_.toRight(new IllegalArgumentException(s"couldn't find playlist $name in Spotify for current user")))
      .flatMap(_.fold(IO.raiseError, IO.pure))
      .map(_.id)

  private def getTrackDetails(token: String, tracks: Seq[PlaylistTrack]): IO[Seq[(PlaylistTrack, SpotifyAudioFeaturesResponse)]] =
    Stream.emits(tracks)
      .evalMap(track => SpotifyRestApi.getAudioFeatures(token, track.id).map(audio => (track, audio)))
      .compile
      .toList
}

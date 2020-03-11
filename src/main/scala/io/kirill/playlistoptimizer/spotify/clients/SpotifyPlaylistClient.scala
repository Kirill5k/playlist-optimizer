package io.kirill.playlistoptimizer.spotify.clients

import cats.effect.IO
import fs2.Stream
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import io.kirill.playlistoptimizer.playlist.{Playlist, PlaylistSource}
import io.kirill.playlistoptimizer.spotify.clients.api.SpotifyResponse.{PlaylistTrack, SpotifyAudioFeaturesResponse}
import io.kirill.playlistoptimizer.spotify.clients.api.{SpotifyMapper, SpotifyRestApi}
import sttp.client.{NothingT, SttpBackend}

private[spotify] class SpotifyPlaylistClient(implicit val sc: SpotifyConfig, val b: SttpBackend[IO, Nothing, NothingT]) {

  def findPlaylistByName(token: String, playlistName: String): IO[Playlist] = {
    for {
      playlistId <- getPlaylistId(token, playlistName)
      playlist <- SpotifyRestApi.getPlaylist(token, playlistId)
      playListTracks = playlist.tracks.items.map(_.track)
      tracksDetails <- getTrackDetails(token, playListTracks)
      tracks = tracksDetails.map(details => SpotifyMapper.toDomain(details._1, details._2)).toVector
    } yield Playlist(playlist.name, playlist.description, PlaylistSource.Spotify, tracks)
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

package io.kirill.playlistoptimizer.clients

import cats.effect.IO
import fs2.Stream
import io.kirill.playlistoptimizer.clients.spotify.{SpotifyApi, SpotifyMapper}
import io.kirill.playlistoptimizer.clients.spotify.SpotifyResponse.{AudioAnalysisTrack, PlaylistTrack, SpotifyAudioFeaturesResponse}
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import io.kirill.playlistoptimizer.domain.{Playlist, PlaylistSource}
import sttp.client.{NothingT, SttpBackend}

private[clients] class SpotifyClient(implicit val c: SpotifyConfig, val b: SttpBackend[IO, Nothing, NothingT]) extends ApiClient[IO] {

  override def findPlaylistByName(playlistName: String): IO[Playlist] = {
    for {
      token <- SpotifyApi.authenticateClient.map(_.access_token)
      playlistId <- getPlaylistId(token, playlistName)
      playlist <- SpotifyApi.getPlaylist(token, playlistId)
      playListTracks = playlist.tracks.items.map(_.track)
      tracksDetails <- getTrackDetails(token, playListTracks)
      tracks = tracksDetails.map(details => SpotifyMapper.toDomain(details._1, details._2)).toVector
    } yield Playlist(playlist.name, playlist.description, PlaylistSource.Spotify, tracks)
  }

  private def getPlaylistId(token: String, name: String): IO[String] =
    SpotifyApi.getUserPlaylists(token, c.auth.userId)
      .map(_.items.find(_.name.equalsIgnoreCase(name)))
      .map(_.toRight(new IllegalArgumentException(s"couldn't find playlist $name in Spotify for user ${c.auth.userId}")))
      .flatMap(_.fold(IO.raiseError, IO.pure))
      .map(_.id)

  private def getTrackDetails(token: String, tracks: Seq[PlaylistTrack]): IO[Seq[(PlaylistTrack, SpotifyAudioFeaturesResponse)]] =
    Stream.emits(tracks)
      .evalMap(track => SpotifyApi.getAudioFeatures(token, track.id).map(audio => (track, audio)))
      .compile
      .toList

  override def savePlaylist(playlist: Playlist): IO[Unit] = ???
}

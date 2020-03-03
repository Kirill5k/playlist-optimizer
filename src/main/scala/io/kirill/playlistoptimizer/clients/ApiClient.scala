package io.kirill.playlistoptimizer.clients

import cats.effect.IO
import fs2.Stream
import io.kirill.playlistoptimizer.clients.spotify.{SpotifyApi, SpotifyMapper}
import io.kirill.playlistoptimizer.clients.spotify.SpotifyResponse._
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import io.kirill.playlistoptimizer.domain.{Playlist, PlaylistSource, Track}
import sttp.client.{NothingT, SttpBackend}

trait ApiClient[F[_]] {
  def findPlaylistByName(playlistName: String): F[Playlist]
}

object ApiClient {

  def spotifyClient(implicit S: SpotifyConfig, B: SttpBackend[IO, Nothing, NothingT]): ApiClient[IO] = new ApiClient[IO] {
    val userId = "e1hyivjak3qiptaiksmlig3c4"

    override def findPlaylistByName(playlistName: String): IO[Playlist] = {
      for {
        token <- SpotifyApi.authenticate.map(_.access_token)
        playlistId <- getPlaylistId(token, playlistName)
        playlist <- SpotifyApi.getPlaylist(token, playlistId)
        playListTracks = playlist.tracks.items.map(_.track)
        tracksDetails <- getTrackDetails(token, playListTracks)
        tracks = tracksDetails.map(t => SpotifyMapper.toDomain(t._1, t._2)).toVector
      } yield Playlist(playlist.name, playlist.description, PlaylistSource.Spotify, tracks)
    }

    private def getPlaylistId(token: String, name: String): IO[String] =
      SpotifyApi.getUserPlaylists(token, userId)
        .map(_.items.find(_.name.equalsIgnoreCase(name)))
        .map(_.toRight(new RuntimeException(s"couldn't find playlist $name in Spotify for user $userId")))
        .flatMap(_.fold(IO.raiseError, IO.pure))
        .map(_.id)

    private def getTrackDetails(token: String, tracks: Seq[PlaylistTrack]): IO[Seq[(PlaylistTrack, AudioAnalysisTrack)]] =
      Stream.emits(tracks)
        .evalMap(track => SpotifyApi.getAudioAnalysis(token, track.id).map(audio => (track, audio.track)))
        .compile
        .toList
  }
}

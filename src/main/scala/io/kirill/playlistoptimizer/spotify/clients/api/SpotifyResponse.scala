package io.kirill.playlistoptimizer.spotify.clients.api

sealed trait SpotifyResponse

object SpotifyResponse {
  final case class SpotifyAuthResponse(access_token: String, token_type: String, expires_in: Int, scope: String, refresh_token: String) extends SpotifyResponse

  final case class SpotifyAuthRefreshResponse(access_token: String, token_type: String, expires_in: Int, scope: String) extends SpotifyResponse

  final case class PlaylistsItem(id: String, name: String)
  final case class SpotifyPlaylistsResponse(items: Seq[PlaylistsItem], total: Int) extends SpotifyResponse

  final case class PlaylistTrackArtist(id: String, name: String)
  final case class PlaylistTrackAlbum(id: String, album_type: String, name: String, release_date: Option[String], release_date_precision: Option[String])
  final case class PlaylistTrackUrls(spotify: String)
  final case class PlaylistTrack(id: String, name: String, album: PlaylistTrackAlbum, artists: Seq[PlaylistTrackArtist], popularity: Double, uri: String, external_urls: PlaylistTrackUrls)
  final case class PlaylistItem(track: PlaylistTrack)
  final case class PlaylistTracks(items: IndexedSeq[PlaylistItem], total: Int)
  final case class SpotifyPlaylistResponse(id: String, name: String, description: Option[String], tracks: PlaylistTracks) extends SpotifyResponse

  final case class AudioAnalysisTrack(duration: Double, tempo: Double, key: Int, mode: Int)
  final case class SpotifyAudioAnalysisResponse(track: AudioAnalysisTrack) extends SpotifyResponse

  final case class SpotifyAudioFeaturesResponse(key: Int, mode: Int, duration_ms: Double, tempo: Double) extends SpotifyResponse

  final case class SpotifyOperationSuccessResponse(snapshot_id: String) extends SpotifyResponse

  final case class SpotifyUserResponse(id: String, display_name: String, email: String) extends SpotifyResponse
}

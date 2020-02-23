package io.kirill.playlistoptimizer.clients.spotify

sealed trait SpotifyResponse

object SpotifyResponse {
  final case class SpotifyAuthResponse(access_token: String, token_type: String, expires_in: Int, scope: String) extends SpotifyResponse

  final case class PlaylistTrackArtist(id: String, name: String)
  final case class PlaylistTrackAlbum(id: String, album_type: String, name: String)
  final case class PlaylistTrack(id: String, name: String, album: PlaylistTrackAlbum, artists: Seq[PlaylistTrackArtist], popularity: Double)
  final case class PlaylistItem(track: PlaylistTrack)
  final case class PlaylistTracks(items: Seq[PlaylistItem], total: Int)
  final case class SpotifyPlaylistResponse(id: String, name: String, description: String, tracks: PlaylistTracks)

  final case class TrackDetails(duration: Double, tempo: Double, key: Int, mode: Int)
  final case class SpotifyAudioAnalysisResponse(track: TrackDetails) extends SpotifyResponse
}

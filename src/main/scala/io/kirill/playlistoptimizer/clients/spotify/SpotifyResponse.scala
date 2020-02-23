package io.kirill.playlistoptimizer.clients.spotify

sealed trait SpotifyResponse

object SpotifyResponse {
  final case class SpotifyAuthResponse(access_token: String, token_type: String, expires_in: Int, scope: String) extends SpotifyResponse

  final case class TrackDetails(duration: Double, tempo: Double, key: Int, mode: Int)
  final case class SpotifyAudioAnalysisResponse(track: TrackDetails) extends SpotifyResponse
}

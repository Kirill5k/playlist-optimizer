package io.kirill.playlistoptimizer.clients.spotify

sealed trait SpotifyAuthResponse

object SpotifyAuthResponse {
  final case class SpotifyAuthSuccessResponse(access_token: String, token_type: String, expires_in: Int, scope: String) extends SpotifyAuthResponse
  final case class SpotifyAuthErrorResponse(error: String, error_description: String) extends SpotifyAuthResponse
}

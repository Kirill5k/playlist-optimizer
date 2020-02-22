package io.kirill.playlistoptimizer.clients.spotify

sealed trait SpotifyAuthResponse

object SpotifyAuthResponse {
  final case class AuthSuccess(access_token: String, token_type: String, expires_in: Int, scope: String) extends SpotifyAuthResponse
  final case class AuthError(error: String, error_description: String) extends SpotifyAuthResponse
}

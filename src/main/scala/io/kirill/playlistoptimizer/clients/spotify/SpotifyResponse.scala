package io.kirill.playlistoptimizer.clients.spotify

sealed trait SpotifyResponse

object SpotifyResponse {
  final case class SpotifyAuthResponse(access_token: String, token_type: String, expires_in: Int, scope: String) extends SpotifyResponse
}

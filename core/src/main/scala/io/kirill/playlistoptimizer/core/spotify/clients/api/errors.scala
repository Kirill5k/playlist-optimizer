package io.kirill.playlistoptimizer.core.spotify.clients.api


object errors {
  final case class ErrorBody(status: Int, message: String)

  final case class SpotifyAuthError(error: String, error_description: String)
  final case class SpotifyRegularError(error: ErrorBody)
}

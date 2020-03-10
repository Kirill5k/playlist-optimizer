package io.kirill.playlistoptimizer.spotify.clients.api

sealed trait SpotifyError extends Throwable

object SpotifyError {
  final case class ErrorBody(status: Int, message: String)

  final case class SpotifyAuthError(error: String, error_description: String) extends SpotifyError
  final case class SpotifyRegularError(error: ErrorBody) extends SpotifyError
}

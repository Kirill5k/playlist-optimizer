package io.kirill.playlistoptimizer.clients.spotify

sealed trait SpotifyError extends Throwable

object SpotifyError {
  private final case class Error(status: Int, message: String)

  final case class SpotifyAuthError(error: String, error_description: String) extends SpotifyError
  final case class SpotifyRegularError(error: Error) extends SpotifyError
}

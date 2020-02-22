package io.kirill.playlistoptimizer.domain

sealed trait ApiClientError extends Throwable

object ApiClientError {
  final case class JsonParsingError(message: String) extends ApiClientError
  final case class AuthError(message: String) extends ApiClientError
}

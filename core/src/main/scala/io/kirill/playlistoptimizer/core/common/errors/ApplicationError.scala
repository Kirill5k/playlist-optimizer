package io.kirill.playlistoptimizer.core.common.errors

sealed trait ApplicationError extends Throwable {
  def message: String
}

object ApplicationError {
  final case class AuthenticationRequiredError(message: String) extends ApplicationError
}

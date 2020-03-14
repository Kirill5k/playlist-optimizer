package io.kirill.playlistoptimizer.common.errors

sealed trait ApplicationError extends Throwable {
  def message: String
}

object ApplicationError {
  final case class UnauthorizedError(message: String) extends ApplicationError
}

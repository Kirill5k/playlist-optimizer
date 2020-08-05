package io.kirill.playlistoptimizer.core.common

import io.kirill.playlistoptimizer.core.optimizer.OptimizationId
import pdi.jwt.JwtAlgorithm

object errors {

  sealed trait ApplicationError extends Throwable {
    def message: String
    override def getMessage: String = message
  }

  sealed trait AuthError extends ApplicationError
  sealed trait NotFoundError extends ApplicationError
  sealed trait BadRequestError extends ApplicationError
  sealed trait ForbiddenError extends ApplicationError

  final case object MissingSessionCookie extends ForbiddenError {
    val message = "missing session cookie"
  }

  final case class JwtDecodeError(message: String) extends ForbiddenError

  final case class SpotifyApiError(message: String) extends ApplicationError

  final case class SpotifyPlaylistNotFound(name: String) extends NotFoundError {
    val message = s"""couldn't find playlist "$name" in Spotify for current user"""
  }

  final case class OptimizationNotFound(id: OptimizationId) extends NotFoundError {
    val message = s"optimization with id ${id.value} does not exist"
  }

  final case class AuthenticationRequiredError(message: String) extends ForbiddenError

  final case class UnexpectedPlaylistSource(source: String) extends BadRequestError {
    val message = s"unrecognized playlist source $source"
  }

  final case class InvalidMode(number: Int) extends BadRequestError {
    val message = s"couldn't find mode with number $number"
  }

  final case class InvalidKey(keyNumber: Int, mode: Int) extends BadRequestError {
    val message = s"couldn't find key with number $keyNumber and mode number $mode"
  }

  final case class InvalidJwtEncryptionAlgorithm(alg: JwtAlgorithm) extends ApplicationError {
    val message = s"unrecognized jwt encryption algorithm $alg"
  }
}

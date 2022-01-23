package io.kirill.playlistoptimizer.core.common

import io.kirill.playlistoptimizer.domain.optimization.OptimizationId
import pdi.jwt.JwtAlgorithm

object errors:

  sealed trait ApplicationError extends Throwable:
    def message: String
    override def getMessage: String = message

  sealed trait AuthError       extends ApplicationError
  sealed trait NotFoundError   extends ApplicationError
  sealed trait BadRequestError extends ApplicationError
  sealed trait ForbiddenError  extends ApplicationError
  sealed trait InternalError   extends ApplicationError

  case object MissingSpotifySessionCookie extends ForbiddenError:
    val message = "missing spotify session cookie"

  case object MissingUserSessionCookie extends ApplicationError:
    val message = "missing user session cookie"

  final case class JwtDecodeError(message: String) extends ForbiddenError

  final case class SpotifyApiError(message: String) extends InternalError

  final case class SpotifyPlaylistNotFound(name: String) extends NotFoundError:
    val message = s"""couldn't find playlist "$name" in Spotify for current user"""

  final case class SpotifyTrackNotFound(name: String) extends NotFoundError:
    val message = s"""couldn't find track "$name" in Spotify"""

  final case class OptimizationNotFound(id: OptimizationId) extends NotFoundError:
    val message = s"optimization with id $id does not exist"

  final case class AuthenticationRequiredError(message: String) extends ForbiddenError

  final case class UnexpectedPlaylistSource(source: String) extends BadRequestError:
    val message = s"unrecognized playlist source $source"

  final case class InvalidMode(number: Int) extends BadRequestError:
    val message = s"couldn't find mode with number $number"

  final case class InvalidKey(keyNumber: Int, mode: Int) extends BadRequestError:
    val message = s"couldn't find key with number $keyNumber and mode number $mode"

  final case class MissingRequiredQueryParam(name: String) extends BadRequestError:
    val message = s"query parameter $name is required to make this request"

  final case class InvalidJwtEncryptionAlgorithm(alg: JwtAlgorithm) extends ApplicationError:
    val message = s"unrecognized jwt encryption algorithm $alg"

  final case class CalculationError(message: String) extends ApplicationError

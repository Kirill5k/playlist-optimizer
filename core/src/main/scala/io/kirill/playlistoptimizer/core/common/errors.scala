package io.kirill.playlistoptimizer.core.common

import io.kirill.playlistoptimizer.core.playlist.PlaylistOptimizer.OptimizationId

object errors {

  sealed trait ApplicationError extends Throwable {
    def message: String
  }

  sealed trait NotFoundError extends ApplicationError
  sealed trait BadRequestError extends ApplicationError

  final case class SpotifyPlaylistNotFound(name: String) extends NotFoundError {
    val message = s"""couldn't find playlist "$name" in Spotify for current user"""
  }

  final case class OptimizationNotFound(id: OptimizationId) extends NotFoundError {
    val message = s"optimization with id ${id.value} does not exist"
  }

  final case class AuthenticationRequiredError(message: String) extends ApplicationError

  final case class UnexpectedPlaylistSource(source: String) extends BadRequestError {
    val message = s"unrecognized playlist source $source"
  }

  final case class InvalidMode(number: Int) extends BadRequestError {
    val message = s"couldn't find mode with number $number"
  }

  final case class InvalidKey(keyNumber: Int, mode: Int) extends BadRequestError {
    val message = s"couldn't find key with number $keyNumber and mode number $mode"
  }
}

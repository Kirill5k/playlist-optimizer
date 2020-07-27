package io.kirill.playlistoptimizer.core.common

import io.kirill.playlistoptimizer.core.optimizer.Optimizer.OptimizationId

object errors {

  sealed trait ApplicationError extends Throwable {
    def message: String
  }

  final case class OptimizationNotFound(id: OptimizationId) extends ApplicationError {
    val message = s"optimization with id ${id.value} does not exist"
  }

  final case class AuthenticationRequiredError(message: String) extends ApplicationError

  final case class UnexpectedPlaylistSource(source: String) extends ApplicationError {
    val message = s"unrecognized playlist source $source"
  }

  final case class InvalidMode(number: Int) extends ApplicationError {
    val message = s"couldn't find mode with number $number"
  }

  final case class InvalidKey(keyNumber: Int, mode: Int) extends ApplicationError {
    val message = s"couldn't find key with number $keyNumber and mode number $mode"
  }
}

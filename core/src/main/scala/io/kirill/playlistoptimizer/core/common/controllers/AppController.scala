package io.kirill.playlistoptimizer.core.common.controllers

import cats.effect._
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.common.errors.AuthenticationRequiredError
import org.http4s.{HttpRoutes, MessageFailure, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._

trait AppController[F[_]] extends Http4sDsl[F] {
  import AppController._

  def routes(implicit cs: ContextShift[F], s: Sync[F], l: Logger[F]): HttpRoutes[F]

  protected def withErrorHandling(
      response: => F[Response[F]]
  )(
      implicit s: Sync[F],
      l: Logger[F]
  ): F[Response[F]] =
    response.handleErrorWith {
      case AuthenticationRequiredError(message) =>
        l.error(s"authentication error: ${message}") *>
          Forbidden(ErrorResponse(message).asJson)
      case error: MessageFailure =>
        l.error(error)(s"error parsing json: ${error.getMessage()}") *>
          BadRequest(ErrorResponse(error.getMessage()).asJson)
      case error =>
        l.error(error)(s"unexpected error: ${error.getMessage}") *>
          InternalServerError(ErrorResponse(error.getMessage()).asJson)
    }
}

object AppController {
  final case class ErrorResponse(message: String)

  def homeController[F[_]: ContextShift](blocker: Blocker): AppController[F] =
    new HomeController[F](blocker)
}

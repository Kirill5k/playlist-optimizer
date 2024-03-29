package io.kirill.playlistoptimizer.core.common.controllers

import cats.Functor

import java.util.UUID
import cats.data.Kleisli
import cats.effect.*
import cats.implicits.*
import io.circe.Codec
import org.typelevel.log4cats.Logger
import io.kirill.playlistoptimizer.core.common.JsonCodecs
import io.kirill.playlistoptimizer.core.common.controllers.Controller.{ErrorResponse, UserSessionId}
import io.kirill.playlistoptimizer.domain.errors.*
import org.http4s.{HttpRoutes, MessageFailure, Request, RequestCookie, Response, ResponseCookie}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec.*

trait Controller[F[_]] extends Http4sDsl[F] with JsonCodecs {
  val UserSessionCookie = "user-session"

  def routes: HttpRoutes[F]

  def routesWithUserSession(implicit F: Functor[F]): HttpRoutes[F] =
    Kleisli { (req: Request[F]) =>
      val sessionId = req.cookies.find(_.name == UserSessionCookie).fold(UUID.randomUUID().toString)(_.content)
      val res       = routes(req.addCookie(RequestCookie(UserSessionCookie, sessionId)))
      res.map(_.addCookie(ResponseCookie(UserSessionCookie, sessionId, httpOnly = true)))
    }

  protected def withErrorHandling(
      response: => F[Response[F]]
  )(implicit
      F: Sync[F],
      logger: Logger[F]
  ): F[Response[F]] =
    response.handleErrorWith {
      case error: ForbiddenError =>
        logger.error(s"forbidden error: ${error.message}") *>
          Forbidden(ErrorResponse(error.message))
      case error: NotFoundError =>
        logger.error(s"not found error: ${error.message}") *>
          NotFound(ErrorResponse(error.message))
      case error: BadRequestError =>
        logger.error(s"bad request error: ${error.message}") *>
          BadRequest(ErrorResponse(error.message))
      case error: MessageFailure =>
        logger.error(error)(s"error parsing json: ${error.getMessage()}") *>
          BadRequest(ErrorResponse(error.getMessage()))
      case error =>
        logger.error(error)(s"unexpected error: ${error.getMessage}") *>
          InternalServerError(ErrorResponse(error.getMessage))
    }

  protected def getCookie(req: Request[F], name: String): Option[RequestCookie] =
    req.cookies.find(_.name == name)

  protected def getUserSessionIdFromCookie(req: Request[F]): Either[ApplicationError, UserSessionId] =
    getCookie(req, UserSessionCookie)
      .map(c => UserSessionId(c.content))
      .toRight(MissingUserSessionCookie)

}

object Controller:

  final case class ErrorResponse(message: String) derives Codec.AsObject

  opaque type UserSessionId = String
  object UserSessionId:
    def apply(id: String): UserSessionId = id

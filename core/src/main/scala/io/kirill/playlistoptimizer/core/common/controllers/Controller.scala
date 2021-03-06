package io.kirill.playlistoptimizer.core.common.controllers

import cats.Functor

import java.util.UUID
import cats.data.Kleisli
import cats.effect._
import cats.implicits._
import org.typelevel.log4cats.Logger
import io.circe.generic.auto._
import io.kirill.playlistoptimizer.core.common.JsonCodecs
import io.kirill.playlistoptimizer.core.common.controllers.Controller.{ErrorResponse, UserSessionId}
import io.kirill.playlistoptimizer.core.common.errors._
import org.http4s.{HttpRoutes, MessageFailure, Request, RequestCookie, Response, ResponseCookie}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._

trait Controller[F[_]] extends Http4sDsl[F] with JsonCodecs {
  val UserSessionCookie = "user-session"

  def routesWithUserSession(implicit F: Functor[F]): HttpRoutes[F] =
    userSessionMiddleware(routes)

  def routes: HttpRoutes[F]

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

  private def userSessionMiddleware(httpRoutes: HttpRoutes[F])(implicit F: Functor[F]): HttpRoutes[F] =
    Kleisli { (req: Request[F]) =>
      val res =
        if (req.cookies.exists(_.name == UserSessionCookie)) httpRoutes(req)
        else httpRoutes(req.addCookie(RequestCookie(UserSessionCookie, UUID.randomUUID().toString)))
      res.map { r =>
        val userSessionId = req.cookies.find(_.name == UserSessionCookie).fold(UUID.randomUUID().toString)(_.content)
        r.addCookie(ResponseCookie(UserSessionCookie, userSessionId, httpOnly = true))
      }
    }
}

object Controller {

  final case class ErrorResponse(message: String)

  final case class UserSessionId(value: String) extends AnyVal
}

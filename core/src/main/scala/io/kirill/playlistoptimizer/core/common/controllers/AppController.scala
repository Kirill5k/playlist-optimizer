package io.kirill.playlistoptimizer.core.common.controllers

import java.util.UUID

import cats.data.Kleisli
import cats.effect._
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.common.errors._
import org.http4s.{HttpRoutes, MessageFailure, Request, RequestCookie, Response, ResponseCookie}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._

trait AppController[F[_]] extends Http4sDsl[F] {
  import AppController._

  def routesWithUserSession(implicit cs: ContextShift[F], s: Sync[F], l: Logger[F]): HttpRoutes[F] =
    userSessionMiddleware(routes)

  def routes(implicit cs: ContextShift[F], s: Sync[F], l: Logger[F]): HttpRoutes[F]

  protected def withErrorHandling(
      response: => F[Response[F]]
  )(
      implicit s: Sync[F],
      l: Logger[F]
  ): F[Response[F]] =
    response.handleErrorWith {
      case error: ForbiddenError =>
        l.error(s"forbidden error: ${error.message}") *>
          Forbidden(ErrorResponse(error.message).asJson)
      case error: NotFoundError =>
        l.error(s"not found error: ${error.message}") *>
          NotFound(ErrorResponse(error.message).asJson)
      case error: BadRequestError =>
        l.error(s"bad request error: ${error.message}") *>
          BadRequest(ErrorResponse(error.message).asJson)
      case error: MessageFailure =>
        l.error(error)(s"error parsing json: ${error.getMessage()}") *>
          BadRequest(ErrorResponse(error.getMessage()).asJson)
      case error =>
        l.error(error)(s"unexpected error: ${error.getMessage}") *>
          InternalServerError(ErrorResponse(error.getMessage()).asJson)
    }

  protected def getCookie(req: Request[F], name: String): Option[RequestCookie] =
    req.cookies.find(_.name == name)

  protected def getUserSessionCookie(req: Request[F])(implicit s: Sync[F]): F[RequestCookie] =
    s.fromOption(getCookie(req, UserSessionCookie), MissingUserSessionCookie)

  private def userSessionMiddleware[F[_]: Sync](
      httpRoutes: HttpRoutes[F]
  ): HttpRoutes[F] = Kleisli { req: Request[F] =>
    val res =
      if (req.cookies.exists(_.name == UserSessionCookie)) httpRoutes(req)
      else httpRoutes(req.addCookie(RequestCookie(UserSessionCookie, UUID.randomUUID().toString)))
    res.map { r =>
      val userSessionId = req.cookies.find(_.name == UserSessionCookie).fold(UUID.randomUUID().toString)(_.content)
      r.addCookie(ResponseCookie(UserSessionCookie, userSessionId, httpOnly = true))
    }
  }
}

object AppController {
  val UserSessionCookie = "user-session"

  final case class ErrorResponse(message: String)

  def homeController[F[_]: ContextShift](blocker: Blocker): AppController[F] =
    new HomeController[F](blocker)
}

package io.kirill.playlistoptimizer.core

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import io.kirill.playlistoptimizer.core.common.SpotifyConfigBuilder
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client3
import sttp.model.{Header, Method}

import scala.io.Source

trait ApiClientSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  implicit val sc: SpotifyConfig  = SpotifyConfigBuilder.testConfig

  def json(path: String): String = Source.fromResource(path).getLines().toList.mkString

  extension (req: client3.Request[?, ?])
    def isPost: Boolean =
      req.method == Method.POST

    def isGet: Boolean =
      req.method == Method.GET

    def isPut: Boolean =
      req.method == Method.PUT

    def hasBearerToken(token: String): Boolean =
      req.headers.contains(new Header("Authorization", s"Bearer $token"))

    def hasBody(json: String): Boolean =
      req.body.toString.contains(json)

    def hasHost(host: String): Boolean =
      req.uri.host.contains(host)

    def hasPath(path: String): Boolean =
      req.uri.path == path.split("/").filter(_.nonEmpty).toList

    def isGoingTo(url: String): Boolean = {
      val urlParts = url.split("/")
      hasHost(urlParts.head) && req.uri.path.startsWith(urlParts.tail.filter(_.nonEmpty).toList)
    }

    def bodyContains(body: String): Boolean =
      req.body.toString.contains(body)
}

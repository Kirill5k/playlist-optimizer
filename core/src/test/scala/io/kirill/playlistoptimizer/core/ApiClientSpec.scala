package io.kirill.playlistoptimizer.core

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.kirill.playlistoptimizer.core.common.SpotifyConfigBuilder
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.io.Source

trait ApiClientSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  implicit val sc: SpotifyConfig = SpotifyConfigBuilder.testConfig

  def json(path: String): String = Source.fromResource(path).getLines().toList.mkString
}

package io.kirill.playlistoptimizer.spotify.clients

import cats.effect.{ContextShift, IO}
import cats.effect.testing.scalatest.AsyncIOSpec
import io.kirill.playlistoptimizer.configs.SpotifyConfigBuilder
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

class SpotifyAuthClientSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)
  implicit val sc = SpotifyConfigBuilder.testConfig
}

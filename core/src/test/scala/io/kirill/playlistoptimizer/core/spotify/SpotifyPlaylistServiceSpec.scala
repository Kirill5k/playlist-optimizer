package io.kirill.playlistoptimizer.core.spotify

import java.time.Instant
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import io.kirill.playlistoptimizer.core.common.SpotifyConfigBuilder
import io.kirill.playlistoptimizer.core.common.config.SpotifyConfig
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend


class SpotifyPlaylistServiceSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with MockitoSugar {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  implicit val sc: SpotifyConfig = SpotifyConfigBuilder.testConfig
  implicit val testingBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend.stub[IO]
    .whenRequestMatchesPartial {
      case _ => throw new RuntimeException()
    }

  val accessToken = SpotifyAccessToken("access-token", "refresh-token", "user-id", Instant.parse("2020-01-01T00:00:00Z"))

  "A SpotifyPlaylistService" - {


  }
}

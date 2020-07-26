package io.kirill.playlistoptimizer.core.spotify.clients

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SpotifyAccessTokenSpec extends AnyWordSpec with Matchers {
  import SpotifyAuthClient._

  "A SpotifyAccessToken" should {

    "return valid if it has not expired" in {
      val token = SpotifyAccessToken("access", "refresh", "user-id", 1000);

      token.isValid must be (true)
    }

    "return invalid if it has not expired" in {
      val token = SpotifyAccessToken("access", "refresh", "user-id", 0);

      token.isValid must be (false)
    }
  }
}
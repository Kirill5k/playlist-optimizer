package io.kirill.playlistoptimizer.core.spotify

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SpotifyAccessTokenSpec extends AnyWordSpec with Matchers {

  "A SpotifyAccessToken" should {

    "return valid if it has not expired" in {
      val token = SpotifyAccessToken("access", "refresh", "user-id", 1000);

      token.isValid mustBe true
    }

    "return invalid if it has not expired" in {
      val token = SpotifyAccessToken("access", "refresh", "user-id", 0);

      token.isValid mustBe false
    }
  }
}

package io.kirill.playlistoptimizer.playlist

import cats.effect._
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.wordspec.AnyWordSpec

class PlaylistControllerSpec extends AnyWordSpec with MockitoSugar with ArgumentMatchersSugar {

  "A PlaylistController" should {
    val playlistServiceMock = mock[PlaylistService[IO]]
    val playlistController = new PlaylistController[IO] {
      override protected val playlistService: PlaylistService[IO] = playlistServiceMock
    }
  }
}

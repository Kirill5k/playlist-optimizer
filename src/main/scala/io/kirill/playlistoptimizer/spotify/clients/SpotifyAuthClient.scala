package io.kirill.playlistoptimizer.spotify.clients

import java.time.Instant

import cats.effect.IO
import io.kirill.playlistoptimizer.configs.SpotifyConfig
import sttp.client.{NothingT, SttpBackend}

private[clients] case class SpotifyAccessToken(accessToken: String, refreshToken: String, userId: String, expiresAt: Instant) {
  def isValid: Boolean = expiresAt.isBefore(Instant.now())
}

private[spotify] class SpotifyAuthClient(authCode: String)(implicit val sc: SpotifyConfig, val b: SttpBackend[IO, Nothing, NothingT]) {



  def token: IO[String] = ???
}

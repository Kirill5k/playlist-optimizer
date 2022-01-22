package io.kirill.playlistoptimizer.core.common

import java.time.Instant
import cats.effect.IO
import io.circe.generic.auto.*
import io.kirill.playlistoptimizer.core.common.config.JwtConfig
import io.kirill.playlistoptimizer.core.common.errors.{InvalidJwtEncryptionAlgorithm, JwtDecodeError}
import io.kirill.playlistoptimizer.core.common.jwt.JwtEncoder
import io.kirill.playlistoptimizer.core.spotify.SpotifyAccessToken
import io.kirill.playlistoptimizer.domain.CatsIOSpec

class JwtEncoderSpec extends CatsIOSpec {

  val config = JwtConfig("HS256", "secret-key")
  val accessToken = SpotifyAccessToken("access-token", "refresh-token", "user-id", Instant.parse("2020-01-01T00:00:00Z"))
  val jwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhY2Nlc3NUb2tlbiI6ImFjY2Vzcy10b2tlbiIsInJlZnJlc2hUb2tlbiI6InJlZnJlc2gtdG9rZW4iLCJ1c2VySWQiOiJ1c2VyLWlkIiwidmFsaWRVbnRpbCI6IjIwMjAtMDEtMDFUMDA6MDA6MDBaIn0.e14E3Fp-aJDpcs86HYfGAkUQjQwS9d73YjSHhaIxpUw"

  "A CirceJwtEncoder" - {

    "should create jwt token" in {
      val result = for {
        encoder <- JwtEncoder.circeJwtEncoder[IO, SpotifyAccessToken](config)
        jwtToken <- encoder.encode(accessToken)
      } yield jwtToken

      result.asserting(_ must be (jwtToken))
    }

    "should decode jwt token" in {
      val result = for {
        encoder <- JwtEncoder.circeJwtEncoder[IO, SpotifyAccessToken](config)
        accessToken <- encoder.decode(jwtToken)
      } yield accessToken

      result.asserting(_ must be (accessToken))
    }

    "should return error when invalid jwt token" in {
      val result = for {
        encoder <- JwtEncoder.circeJwtEncoder[IO, SpotifyAccessToken](config)
        accessToken <- encoder.decode("foo-bar")
      } yield accessToken

      result.assertThrows[JwtDecodeError]
    }

    "should return error when unexpected json payload" in {
      val result = for {
        encoder <- JwtEncoder.circeJwtEncoder[IO, SpotifyAccessToken](config)
        accessToken <- encoder.decode("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
      } yield accessToken

      result.assertThrows[JwtDecodeError]
    }

    "should return error when unknown algo" in {
      val result = for {
        _ <- JwtEncoder.circeJwtEncoder[IO, SpotifyAccessToken](config.copy(alg = "foo"))
      } yield ()

      result.assertThrows[InvalidJwtEncryptionAlgorithm]
    }
  }
}

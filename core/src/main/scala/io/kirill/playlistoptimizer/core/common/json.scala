package io.kirill.playlistoptimizer.core.common

import java.time.Instant

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.extras.defaults._
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}
import io.kirill.playlistoptimizer.core.optimizer.PlaylistOptimizer.OptimizationId
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

import scala.util.Try

object json extends JsonCodecs {
  implicit def deriveEntityEncoder[F[_]: Applicative, A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
  implicit def deriveEntityDecoder[F[_]: Sync, A: Decoder]: EntityDecoder[F, A]        = jsonOf[F, A]
}

trait JsonCodecs {

  implicit val oidEncoder: Encoder[OptimizationId] = deriveUnwrappedEncoder
  implicit val oidDecoder: Decoder[OptimizationId] = deriveUnwrappedDecoder

  implicit val instantEncode: Encoder[Instant] = Encoder.encodeString.contramap[Instant](_.toString)
  implicit val instantDecoder: Decoder[Instant] = Decoder.decodeString.emapTry{ str => Try(Instant.parse(str))}
}

package io.kirill.playlistoptimizer.core.common

import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}
import io.kirill.playlistoptimizer.core.optimizer.OptimizationId

import java.time.Instant
import scala.util.Try

object json extends JsonCodecs

trait JsonCodecs {

  implicit val oidEncoder: Encoder[OptimizationId] = deriveUnwrappedEncoder
  implicit val oidDecoder: Decoder[OptimizationId] = deriveUnwrappedDecoder

  implicit val instantEncode: Encoder[Instant]  = Encoder.encodeString.contramap[Instant](_.toString)
  implicit val instantDecoder: Decoder[Instant] = Decoder.decodeString.emapTry(str => Try(Instant.parse(str)))
}

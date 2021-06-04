package io.kirill.playlistoptimizer.core.common

import io.circe.{Decoder, Encoder}
import io.kirill.playlistoptimizer.core.optimizer.OptimizationId

import java.time.Instant
import java.util.UUID
import scala.util.Try

object json extends JsonCodecs

trait JsonCodecs {

  implicit val oidEncoder: Encoder[OptimizationId] = Encoder.encodeString.contramap[OptimizationId](_.value.toString)
  implicit val oidDecoder: Decoder[OptimizationId] = Decoder.decodeString.emapTry(str => Try(OptimizationId(UUID.fromString(str))))

  implicit val instantEncode: Encoder[Instant]  = Encoder.encodeString.contramap[Instant](_.toString)
  implicit val instantDecoder: Decoder[Instant] = Decoder.decodeString.emapTry(str => Try(Instant.parse(str)))
}

package io.kirill.playlistoptimizer.core.common

import io.circe.{Decoder, Encoder}
import io.kirill.playlistoptimizer.core.optimizer.OptimizationId

import java.time.Instant
import java.util.UUID
import scala.util.Try

object json extends JsonCodecs

trait JsonCodecs:
  given oidEncoder: Encoder[OptimizationId] = Encoder.encodeString.contramap[OptimizationId](_.toString)
  given oidDecoder: Decoder[OptimizationId] = Decoder.decodeString.emapTry(str => Try(OptimizationId.fromString(str)))

  given instantEncode: Encoder[Instant]  = Encoder.encodeString.contramap[Instant](_.toString)
  given instantDecoder: Decoder[Instant] = Decoder.decodeString.emapTry(str => Try(Instant.parse(str)))

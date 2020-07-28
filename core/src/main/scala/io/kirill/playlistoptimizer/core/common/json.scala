package io.kirill.playlistoptimizer.core.common

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.extras.defaults._
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}
import io.kirill.playlistoptimizer.core.playlist.PlaylistOptimizer.OptimizationId
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

object json extends JsonCodecs {
  implicit def deriveEntityEncoder[F[_]: Applicative, A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
  implicit def deriveEntityDecoder[F[_]: Sync, A: Decoder]: EntityDecoder[F, A]        = jsonOf[F, A]
}

trait JsonCodecs {

  implicit val oidEncoder: Encoder[OptimizationId] = deriveUnwrappedEncoder
  implicit val oidDecoder: Decoder[OptimizationId] = deriveUnwrappedDecoder
}

package io.kirill.playlistoptimizer.core.common

import cats.effect.Sync
import cats.implicits._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.common.config.JwtConfig
import io.kirill.playlistoptimizer.core.common.errors.{InvalidJwtEncryptionAlgorithm, JwtDecodeError}
import pdi.jwt.algorithms.{JwtAsymmetricAlgorithm, JwtECDSAAlgorithm, JwtHmacAlgorithm, JwtRSAAlgorithm}
import pdi.jwt.{JwtAlgorithm, JwtCirce}

object jwt {

  sealed trait JwtEncoder[F[_], A] {
    def encode(token: A): F[String]
    def decode(token: String): F[A]
  }

  class CirceJwtEncoder[F[_]: Sync, A: Encoder: Decoder](
      private val secretKey: String,
      private val alg: JwtAlgorithm
  ) extends JwtEncoder[F, A] {

    private val decodeFunc = alg match {
      case a if JwtAlgorithm.allHmac().contains(a) =>
        (t: String) => JwtCirce.decodeJson(t, secretKey, List(a.asInstanceOf[JwtHmacAlgorithm]))
      case a if JwtAlgorithm.allAsymmetric().contains(a) =>
        (t: String) => JwtCirce.decodeJson(t, secretKey, List(a.asInstanceOf[JwtAsymmetricAlgorithm]))
    }

    override def encode(token: A): F[String] =
      Sync[F].delay(JwtCirce.encode(token.asJson, secretKey, alg))

    override def decode(token: String): F[A] =
      Sync[F].fromEither(decodeFunc(token).toEither.flatMap(_.as[A]).left.map(e => JwtDecodeError(e.getMessage)))
  }

  object JwtEncoder {
    def circeJwtEncoder[F[_]: Sync, A: Encoder: Decoder](config: JwtConfig): F[JwtEncoder[F, A]] =
      JwtAlgorithm.fromString(config.alg.toUpperCase) match {
        case alg if JwtAlgorithm.allHmac().contains(alg) | JwtAlgorithm.allAsymmetric().contains(alg) =>
          Sync[F].delay(new CirceJwtEncoder(config.secret, alg))
        case alg =>
          Sync[F].raiseError(InvalidJwtEncryptionAlgorithm(alg))
      }

  }
}

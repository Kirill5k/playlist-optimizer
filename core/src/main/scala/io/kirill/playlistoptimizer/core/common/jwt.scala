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

import scala.util.{Failure, Try}

object jwt {

  sealed trait JwtEncoder[F[_], A] {
    def encode(token: A): F[String]
    def decode(token: String): F[A]
  }

  private final class CirceJwtEncoder[F[_]: Sync, A: Encoder: Decoder](
      private val encodeFunc: A => String,
      private val decodeFunc: String => Try[Json]
  ) extends JwtEncoder[F, A] {

    override def encode(token: A): F[String] =
      Sync[F].delay(encodeFunc(token))

    override def decode(token: String): F[A] =
      Sync[F].fromEither(decodeFunc(token).toEither.left.map(e => JwtDecodeError(e.getMessage)).flatMap(_.as[A]))
  }

  object JwtEncoder {
    def circeJwtEncoder[F[_]: Sync, A: Encoder: Decoder](config: JwtConfig): F[JwtEncoder[F, A]] = {
        JwtAlgorithm.fromString(config.alg) match {
          case a if JwtAlgorithm.allHmac().contains(a) =>
            Sync[F].delay(new CirceJwtEncoder(
              t => JwtCirce.encode(t.asJson, config.secret, a),
              t => JwtCirce.decodeJson(t, config.secret, List(a.asInstanceOf[JwtHmacAlgorithm]))
            ))
          case a if JwtAlgorithm.allAsymmetric().contains(a) =>
            Sync[F].delay(new CirceJwtEncoder(
              t => JwtCirce.encode(t.asJson, config.secret, a),
              t => JwtCirce.decodeJson(t, config.secret, List(a.asInstanceOf[JwtAsymmetricAlgorithm]))
            ))
          case a  =>
            Sync[F].raiseError(InvalidJwtEncryptionAlgorithm(a))
      }
    }
  }
}

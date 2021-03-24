package io.kirill.playlistoptimizer.core.common

import cats.effect.Sync
import io.circe._
import io.circe.syntax._
import io.kirill.playlistoptimizer.core.common.config.JwtConfig
import io.kirill.playlistoptimizer.core.common.errors.{InvalidJwtEncryptionAlgorithm, JwtDecodeError}
import pdi.jwt.algorithms.{JwtAsymmetricAlgorithm, JwtHmacAlgorithm}
import pdi.jwt.{JwtAlgorithm, JwtCirce}

import scala.util.Failure

object jwt {

  sealed trait JwtEncoder[F[_], A] {
    def encode(token: A): F[String]
    def decode(token: String): F[A]
  }

  private final class CirceJwtEncoder[F[_]: Sync, A: Encoder: Decoder](
      private val secret: String,
      private val alg: JwtAlgorithm
  ) extends JwtEncoder[F, A] {

    private val decodeFunc = alg match {
      case a if JwtAlgorithm.allHmac().contains(a) =>
        (t: String) => JwtCirce.decodeJson(t, secret, List(a.asInstanceOf[JwtHmacAlgorithm]))
      case a if JwtAlgorithm.allAsymmetric().contains(a) =>
        (t: String) => JwtCirce.decodeJson(t, secret, List(a.asInstanceOf[JwtAsymmetricAlgorithm]))
      case a =>
        (_: String) => Failure(InvalidJwtEncryptionAlgorithm(a))
    }

    override def encode(token: A): F[String] =
      Sync[F].delay(JwtCirce.encode(token.asJson, secret, alg))

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

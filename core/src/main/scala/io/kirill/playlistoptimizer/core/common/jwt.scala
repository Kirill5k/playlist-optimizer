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

  trait JwtEncoder[F[_], A] {
    def encode(token: A): F[String]
    def decode(token: String): F[A]
  }

  final private class CirceJwtEncoder[F[_], A: Encoder: Decoder](
      private val secret: String,
      private val alg: JwtAlgorithm
  )(implicit
      F: Sync[F]
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
      F.delay(JwtCirce.encode(token.asJson, secret, alg))

    override def decode(token: String): F[A] =
      F.fromEither(decodeFunc(token).toEither.flatMap(_.as[A]).left.map(e => JwtDecodeError(e.getMessage)))
  }

  object JwtEncoder {
    def circeJwtEncoder[F[_], A: Codec](config: JwtConfig)(implicit
        F: Sync[F]
    ): F[JwtEncoder[F, A]] =
      JwtAlgorithm.fromString(config.alg.toUpperCase) match {
        case alg if JwtAlgorithm.allHmac().contains(alg) | JwtAlgorithm.allAsymmetric().contains(alg) =>
          F.pure(new CirceJwtEncoder(config.secret, alg))
        case alg =>
          F.raiseError(InvalidJwtEncryptionAlgorithm(alg))
      }

  }
}

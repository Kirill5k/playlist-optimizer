package io.kirill.playlistoptimizer.core

import cats.effect.{Async, Resource}
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

trait Resources[F[_]]:
  def backend: SttpBackend[F, Any]

object Resources:
  def make[F[_]: Async]: Resource[F, Resources[F]] =
    Resource.make(AsyncHttpClientCatsBackend[F]())(_.close()).map { b =>
      new Resources[F] {
        override def backend: SttpBackend[F, Any] = b
      }
    }

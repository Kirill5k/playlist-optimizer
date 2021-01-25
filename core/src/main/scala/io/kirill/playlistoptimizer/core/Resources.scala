package io.kirill.playlistoptimizer.core

import cats.effect.{Blocker, Concurrent, ContextShift, Resource}
import cats.implicits._
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.SttpBackend

final case class Resources[F[_]](
    blocker: Blocker,
    backend: SttpBackend[F, Any]
)

object Resources {

  def make[F[_]: Concurrent: ContextShift]: Resource[F, Resources[F]] =
    (Blocker[F], Resource.make(AsyncHttpClientCatsBackend[F]())(_.close())).mapN(Resources.apply[F])
}

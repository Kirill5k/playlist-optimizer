package io.kirill.playlistoptimizer.core

import cats.effect.{Blocker, Concurrent, ContextShift, IO, Resource, Sync}
import cats.implicits._
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.{NothingT, SttpBackend}

final case class Resources[F[_]](
    blocker: Blocker,
    backend: SttpBackend[F, Nothing, NothingT]
)

object Resources {

  def make[F[_]: Concurrent: ContextShift]: Resource[F, Resources[F]] =
    (Blocker[F], Resource.make(AsyncHttpClientCatsBackend[F]())(_.close())).mapN(Resources.apply[F])
}

package io.kirill.playlistoptimizer.core.spotify.clients.api

import io.circe.Codec

object errors {
  final case class ErrorBody(status: Int, message: String) derives Codec.AsObject

  final case class SpotifyAuthError(error: String, error_description: String) derives Codec.AsObject
  final case class SpotifyRegularError(error: ErrorBody) derives Codec.AsObject
}

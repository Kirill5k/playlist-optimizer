package io.kirill.playlistoptimizer.core.spotify.clients.api

import io.circe.*

object requests {

  final case class CreatePlaylistRequest(
      name: String,
      description: Option[String],
      public: Boolean = true,
      collaborative: Boolean = false
  ) derives Encoder.AsObject

  final case class AddTracksToPlaylistRequest(
      uris: Seq[String],
      position: Option[Int]
  ) derives Encoder.AsObject

  final case class ReplaceTracksInPlaylistRequest(
      uris: Seq[String]
  ) derives Encoder.AsObject
}

package io.kirill.playlistoptimizer.core.spotify.clients.api

object requests {

  final case class CreatePlaylistRequest(
      name: String,
      description: Option[String],
      public: Boolean = true,
      collaborative: Boolean = false
  )

  final case class AddTracksToPlaylistRequest(
      uris: Seq[String],
      position: Option[Int]
  )

  final case class ReplaceTracksInPlaylistRequest(
      uris: Seq[String]
  )
}

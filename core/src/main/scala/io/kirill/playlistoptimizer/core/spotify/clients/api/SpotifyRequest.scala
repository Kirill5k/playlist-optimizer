package io.kirill.playlistoptimizer.core.spotify.clients.api

sealed trait SpotifyRequest

object SpotifyRequest {

  case class CreatePlaylistRequest(name: String, description: Option[String], public: Boolean = true, collaborative: Boolean = false) extends SpotifyRequest

  case class AddTracksToPlaylistRequest(uris: Seq[String], position: Option[Int]) extends SpotifyRequest

  case class ReplaceTracksInPlaylistRequest(uris: Seq[String]) extends SpotifyRequest
}

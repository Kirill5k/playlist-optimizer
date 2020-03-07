package io.kirill.playlistoptimizer.clients.spotify

sealed trait SpotifyRequest

object SpotifyRequest {

  case class AddTracksToPlaylistRequest(uris: Seq[String], position: Option[Int]) extends SpotifyRequest
}

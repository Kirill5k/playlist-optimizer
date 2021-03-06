package io.kirill.playlistoptimizer.core.spotify.clients.api


object responses {
  final case class ExternalUrls(spotify: String)
  final case class ExternalIds(isrc: Option[String])
  final case class AlbumImage(url: String, height: Int, width: Int)

  final case class SpotifyAuthResponse(
      access_token: String,
      token_type: String,
      expires_in: Int,
      scope: String,
      refresh_token: String
  )

  final case class SpotifyAuthRefreshResponse(
      access_token: String,
      token_type: String,
      expires_in: Int,
      scope: String
  )

  final case class PlaylistsItem(id: String, name: String)
  final case class SpotifyPlaylistsResponse(items: List[PlaylistsItem], total: Int)

  final case class PlaylistTrackArtist(id: String, name: String)

  final case class PlaylistTrackAlbum(
      id: String,
      album_type: String,
      name: String,
      release_date: Option[String],
      release_date_precision: Option[String],
      images: List[AlbumImage]
  )

  final case class PlaylistTrack(
      id: String,
      name: String,
      album: PlaylistTrackAlbum,
      artists: List[PlaylistTrackArtist],
      popularity: Double,
      uri: String,
      external_urls: ExternalUrls,
      external_ids: ExternalIds
  )
  final case class PlaylistItem(track: PlaylistTrack)
  final case class PlaylistTracks(items: IndexedSeq[PlaylistItem], total: Int)
  final case class SpotifyPlaylistResponse(
      id: String,
      name: String,
      description: Option[String],
      tracks: PlaylistTracks
  )

  final case class AudioAnalysisTrack(duration: Double, tempo: Double, key: Int, mode: Int)
  final case class SpotifyAudioAnalysisResponse(track: AudioAnalysisTrack)

  final case class SpotifyAudioFeaturesResponse(
      id: String,
      key: Int,
      mode: Int,
      duration_ms: Double,
      tempo: Double,
      energy: Double,
      danceability: Double
  )

  final case class SpotifyMultipleAudioFeaturesResponse(
      audio_features: List[SpotifyAudioFeaturesResponse]
  )

  final case class SpotifyOperationSuccessResponse(snapshot_id: String)

  final case class SpotifyUserResponse(id: String, display_name: String)

  final case class SpotifyTrackSearchResults(items: List[PlaylistTrack], total: Int)
  final case class SpotifySearchResponse(tracks: SpotifyTrackSearchResults)
}

package io.kirill.playlistoptimizer.core.spotify.clients.api

import io.circe.Codec

object responses {
  final case class ExternalUrls(spotify: String) derives Codec.AsObject
  final case class ExternalIds(isrc: Option[String]) derives Codec.AsObject
  final case class AlbumImage(url: String, height: Int, width: Int) derives Codec.AsObject

  final case class SpotifyAuthResponse(
      access_token: String,
      token_type: String,
      expires_in: Int,
      scope: String,
      refresh_token: String
  ) derives Codec.AsObject

  final case class SpotifyAuthRefreshResponse(
      access_token: String,
      token_type: String,
      expires_in: Int,
      scope: String
  ) derives Codec.AsObject

  final case class PlaylistsItem(id: String, name: String) derives Codec.AsObject
  final case class SpotifyPlaylistsResponse(items: List[PlaylistsItem], total: Int) derives Codec.AsObject

  final case class PlaylistTrackArtist(id: String, name: String) derives Codec.AsObject

  final case class PlaylistTrackAlbum(
      id: String,
      album_type: String,
      name: String,
      release_date: Option[String],
      release_date_precision: Option[String],
      images: List[AlbumImage]
  ) derives Codec.AsObject

  final case class PlaylistTrack(
      id: String,
      name: String,
      album: PlaylistTrackAlbum,
      artists: List[PlaylistTrackArtist],
      popularity: Double,
      uri: String,
      external_urls: ExternalUrls,
      external_ids: ExternalIds
  ) derives Codec.AsObject

  final case class PlaylistItem(track: PlaylistTrack) derives Codec.AsObject
  final case class PlaylistTracks(items: IndexedSeq[PlaylistItem], total: Int) derives Codec.AsObject

  final case class SpotifyPlaylistResponse(
      id: String,
      name: String,
      description: Option[String],
      tracks: PlaylistTracks
  ) derives Codec.AsObject

  final case class AudioAnalysisTrack(duration: Double, tempo: Double, key: Int, mode: Int) derives Codec.AsObject
  final case class SpotifyAudioAnalysisResponse(track: AudioAnalysisTrack) derives Codec.AsObject

  final case class SpotifyAudioFeaturesResponse(
      id: String,
      key: Int,
      mode: Int,
      duration_ms: Double,
      tempo: Double,
      energy: Double,
      danceability: Double
  ) derives Codec.AsObject

  final case class SpotifyMultipleAudioFeaturesResponse(
      audio_features: List[SpotifyAudioFeaturesResponse]
  ) derives Codec.AsObject

  final case class SpotifyOperationSuccessResponse(snapshot_id: String) derives Codec.AsObject

  final case class SpotifyUserResponse(id: String, display_name: String) derives Codec.AsObject

  final case class SpotifyTrackSearchResults(items: List[PlaylistTrack], total: Int) derives Codec.AsObject
  final case class SpotifySearchResponse(tracks: SpotifyTrackSearchResults) derives Codec.AsObject
}

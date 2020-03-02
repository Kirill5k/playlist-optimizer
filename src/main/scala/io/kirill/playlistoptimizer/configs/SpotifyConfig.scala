package io.kirill.playlistoptimizer.configs

final case class SpotifyAuthConfig(baseUrl: String, tokenPath: String, clientId: String, clientSecret: String)

final case class SpotifyApiConfig(baseUrl: String, usersPath: String, playlistsPath: String, audioAnalysisPath: String)

final case class SpotifyConfig(auth: SpotifyAuthConfig, api: SpotifyApiConfig)

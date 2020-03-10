package io.kirill.playlistoptimizer.configs

final case class SpotifyAuthConfig(
                                    baseUrl: String,
                                    authorizationPath: String,
                                    tokenPath: String,
                                    clientId: String,
                                    clientSecret: String,
                                    redirectUri: String
                                  )

final case class SpotifyApiConfig(
                                   baseUrl: String,
                                   usersPath: String,
                                   playlistsPath: String,
                                   audioAnalysisPath: String,
                                   audioFeaturesPath: String
                                 )

final case class SpotifyConfig(auth: SpotifyAuthConfig, api: SpotifyApiConfig)

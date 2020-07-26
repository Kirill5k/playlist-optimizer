package io.kirill.playlistoptimizer.common.configs

final case class SpotifyConfig(
                                authUrl: String,
                                restUrl: String,
                                authorizationPath: String,
                                clientId: String,
                                clientSecret: String,
                                redirectUri: String
                              )

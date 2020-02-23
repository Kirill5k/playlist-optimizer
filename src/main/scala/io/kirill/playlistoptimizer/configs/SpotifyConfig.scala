package io.kirill.playlistoptimizer.configs

final case class SpotifyConfig(
                                baseUrl: String,
                                authPath: String,
                                audioAnalysisPath: String,
                                clientId: String,
                                clientSecret: String
                              )

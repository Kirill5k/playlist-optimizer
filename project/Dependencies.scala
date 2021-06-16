import sbt._

object Dependencies {

  private object Versions {
    val pureConfig = "0.16.0"
    val circe      = "0.14.1"
    val sttp       = "3.3.6"
    val http4s     = "1.0.0-M23"
    val jwt        = "5.0.0"
    val logback    = "1.2.3"
    val log4cats   = "2.1.1"

    val mockito           = "1.16.37"
    val scalatest         = "3.2.9"
    val scalameter        = "0.21"
    val catsEffectTesting = "1.1.1"
  }

  private object Libraries {
    val pureconfigCore = "com.github.pureconfig" %% "pureconfig-core" % Versions.pureConfig

    val logback  = "ch.qos.logback" % "logback-classic" % Versions.logback
    val log4cats = "org.typelevel" %% "log4cats-slf4j"  % Versions.log4cats

    val circeCore          = "io.circe" %% "circe-core"           % Versions.circe
    val circeGeneric       = "io.circe" %% "circe-generic"        % Versions.circe
    val circeParser        = "io.circe" %% "circe-parser"         % Versions.circe

    val sttpCore        = "com.softwaremill.sttp.client3" %% "core"                           % Versions.sttp
    val sttpCirce       = "com.softwaremill.sttp.client3" %% "circe"                          % Versions.sttp
    val sttpCatsBackend = "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % Versions.sttp

    val http4sCore   = "org.http4s" %% "http4s-core"         % Versions.http4s
    val http4sDsl    = "org.http4s" %% "http4s-dsl"          % Versions.http4s
    val http4sServer = "org.http4s" %% "http4s-server"       % Versions.http4s
    val http4sBlaze  = "org.http4s" %% "http4s-blaze-server" % Versions.http4s
    val http4sCirce  = "org.http4s" %% "http4s-circe"        % Versions.http4s

    val jwtCirce = "com.pauldijou" %% "jwt-circe" % Versions.jwt

    val mockitoCore      = "org.mockito"       %% "mockito-scala"                 % Versions.mockito
    val mockitoScalatest = "org.mockito"       %% "mockito-scala-scalatest"       % Versions.mockito
    val scalatest        = "org.scalatest"     %% "scalatest"                     % Versions.scalatest
    val scalameter       = "com.storm-enroute" %% "scalameter"                    % Versions.scalameter
    val catsEffectTest   = "org.typelevel"     %% "cats-effect-testing-scalatest" % Versions.catsEffectTesting
  }

  lazy val core = Seq(
    Libraries.pureconfigCore,
    Libraries.logback,
    Libraries.log4cats,
    Libraries.circeCore,
    Libraries.circeGeneric,
    Libraries.circeParser,
    Libraries.sttpCore,
    Libraries.sttpCirce,
    Libraries.sttpCatsBackend,
    Libraries.http4sCore,
    Libraries.http4sDsl,
    Libraries.http4sServer,
    Libraries.http4sBlaze,
    Libraries.http4sCirce,
    Libraries.jwtCirce.cross(CrossVersion.for3Use2_13)
  )

  lazy val test = Seq(
    Libraries.mockitoCore.cross(CrossVersion.for3Use2_13)      % Test,
    Libraries.mockitoScalatest.cross(CrossVersion.for3Use2_13) % Test,
    Libraries.scalatest        % Test,
    Libraries.scalameter.cross(CrossVersion.for3Use2_13)       % Test,
    Libraries.catsEffectTest   % Test
  )
}

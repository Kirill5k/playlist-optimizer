import sbt._

object Dependencies {

  object Versions {
    lazy val fs2        = "2.5.0"
    lazy val pureConfig = "0.14.0"
    lazy val circe      = "0.13.0"
    lazy val sttp       = "3.0.0"
    lazy val http4s     = "0.21.1"
    lazy val jwt        = "4.2.0"
    lazy val logback    = "1.2.3"
    lazy val log4cats   = "1.1.1"

    lazy val mockito    = "1.10.3"
    lazy val scalatest  = "3.2.0"
    lazy val scalameter = "0.19"
  }

  object Libraries {
    lazy val pureconfigCore = "com.github.pureconfig" %% "pureconfig"             % Versions.pureConfig
    lazy val pureconfigCats = "com.github.pureconfig" %% "pureconfig-cats-effect" % Versions.pureConfig

    lazy val logback  = "ch.qos.logback"    % "logback-classic" % Versions.log4cats
    lazy val log4cats = "io.chrisdavenport" %% "log4cats-slf4j" % Versions.log4cats

    lazy val fs2 = "co.fs2" %% "fs2-core" % Versions.fs2

    lazy val circeCore          = "io.circe" %% "circe-core"           % Versions.circe
    lazy val circeLiteral       = "io.circe" %% "circe-literal"        % Versions.circe
    lazy val circeGeneric       = "io.circe" %% "circe-generic"        % Versions.circe
    lazy val circeGenericExtras = "io.circe" %% "circe-generic-extras" % Versions.circe
    lazy val circeParser        = "io.circe" %% "circe-parser"         % Versions.circe

    lazy val sttpCore        = "com.softwaremill.sttp.client3" %% "core"                           % Versions.sttp
    lazy val sttpCirce       = "com.softwaremill.sttp.client3" %% "circe"                          % Versions.sttp
    lazy val sttpCatsBackend = "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % Versions.sttp

    lazy val http4sCore   = "org.http4s" %% "http4s-core"         % Versions.http4s
    lazy val http4sDsl    = "org.http4s" %% "http4s-dsl"          % Versions.http4s
    lazy val http4sServer = "org.http4s" %% "http4s-server"       % Versions.http4s
    lazy val http4sBlaze  = "org.http4s" %% "http4s-blaze-server" % Versions.http4s
    lazy val http4sCirce  = "org.http4s" %% "http4s-circe"        % Versions.http4s

    lazy val jwtCirce = "com.pauldijou" %% "jwt-circe" % Versions.jwt

    lazy val mockitoCore      = "org.mockito"       %% "mockito-scala"                 % Versions.mockito
    lazy val mockitoScalatest = "org.mockito"       %% "mockito-scala-scalatest"       % Versions.mockito
    lazy val scalatest        = "org.scalatest"     %% "scalatest"                     % Versions.scalatest
    lazy val scalameter       = "com.storm-enroute" %% "scalameter"                    % Versions.scalameter
    lazy val catsEffectTest   = "com.codecommit"    %% "cats-effect-testing-scalatest" % "0.4.0"
  }

  lazy val core = Seq(
    Libraries.pureconfigCats,
    Libraries.pureconfigCore,
    Libraries.logback,
    Libraries.log4cats,
    Libraries.fs2,
    Libraries.circeCore,
    Libraries.circeLiteral,
    Libraries.circeGeneric,
    Libraries.circeGenericExtras,
    Libraries.circeParser,
    Libraries.sttpCore,
    Libraries.sttpCirce,
    Libraries.sttpCatsBackend,
    Libraries.http4sCore,
    Libraries.http4sDsl,
    Libraries.http4sServer,
    Libraries.http4sBlaze,
    Libraries.http4sCirce,
    Libraries.jwtCirce
  )

  lazy val test = Seq(
    Libraries.mockitoCore      % Test,
    Libraries.mockitoScalatest % Test,
    Libraries.scalatest        % Test,
    Libraries.scalameter       % Test,
    Libraries.catsEffectTest   % Test
  )
}

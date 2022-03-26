import sbt._

object Dependencies {
  private object Versions {
    val fs2        = "3.2.5"
    val cats       = "2.7.0"
    val pureConfig = "0.17.1"
    val circe      = "0.14.1"
    val sttp       = "3.5.1"
    val http4s     = "0.23.11"
    val jwt        = "9.0.5"
    val logback    = "1.2.10"
    val log4cats   = "2.2.0"

    val scalameter        = "0.21"
    val catsEffectTesting = "1.4.0"

    val scalatest = "3.2.11"
    val mockito   = "3.2.10.0"
  }

  private object Libraries {
    val fs2            = "co.fs2"                %% "fs2-core"        % Versions.fs2
    val catsFree       = "org.typelevel"         %% "cats-free"       % Versions.cats
    val pureconfigCore = "com.github.pureconfig" %% "pureconfig-core" % Versions.pureConfig

    val logback  = "ch.qos.logback" % "logback-classic" % Versions.logback
    val log4cats = "org.typelevel" %% "log4cats-slf4j"  % Versions.log4cats

    val circeCore    = "io.circe" %% "circe-core"    % Versions.circe
    val circeGeneric = "io.circe" %% "circe-generic" % Versions.circe
    val circeParser  = "io.circe" %% "circe-parser"  % Versions.circe

    val sttpCore        = "com.softwaremill.sttp.client3" %% "core"                           % Versions.sttp
    val sttpCirce       = "com.softwaremill.sttp.client3" %% "circe"                          % Versions.sttp
    val sttpCatsBackend = "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % Versions.sttp

    val http4sCore   = "org.http4s" %% "http4s-core"         % Versions.http4s
    val http4sDsl    = "org.http4s" %% "http4s-dsl"          % Versions.http4s
    val http4sServer = "org.http4s" %% "http4s-server"       % Versions.http4s
    val http4sBlaze  = "org.http4s" %% "http4s-blaze-server" % Versions.http4s
    val http4sCirce  = "org.http4s" %% "http4s-circe"        % Versions.http4s

    val jwt = "com.github.jwt-scala" %% "jwt-circe" % Versions.jwt

    val mockito        = "org.scalatestplus" %% "mockito-3-4"                   % Versions.mockito
    val scalatest      = "org.scalatest"     %% "scalatest"                     % Versions.scalatest
    val scalameter     = "com.storm-enroute" %% "scalameter"                    % Versions.scalameter cross (CrossVersion.for3Use2_13)
    val catsEffectTest = "org.typelevel"     %% "cats-effect-testing-scalatest" % Versions.catsEffectTesting
  }

  lazy val core = Seq(
    Libraries.pureconfigCore,
    Libraries.logback,
    Libraries.log4cats,
    Libraries.sttpCore,
    Libraries.sttpCirce,
    Libraries.sttpCatsBackend,
    Libraries.http4sCore,
    Libraries.http4sDsl,
    Libraries.http4sServer,
    Libraries.http4sBlaze,
    Libraries.http4sCirce
  )

  lazy val test = Seq(
    Libraries.mockito        % Test,
    Libraries.scalatest      % Test,
    Libraries.catsEffectTest % Test
  )

  lazy val benchmark = Seq(
    Libraries.scalameter % Test
  )

  lazy val algorithm = Seq(
    Libraries.catsFree,
    Libraries.fs2
  )

  lazy val domain = Seq(
    Libraries.circeCore,
    Libraries.circeGeneric,
    Libraries.circeParser,
    Libraries.jwt
  )
}

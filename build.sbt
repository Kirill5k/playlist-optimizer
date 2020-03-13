name := "playlist-optimizer"
organization := "io.kirill"
version := "1.0"

scalaVersion := "2.13.1"

lazy val pureConfigVersion = "0.12.3"
lazy val circeVersion = "0.12.3"
lazy val mockitoVersion = "1.10.3"
lazy val sttpVersion = "2.0.5"
lazy val http4sVersion = "0.21.1"

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
  "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigVersion,

  "org.typelevel" %% "cats-core" % "2.1.0",
  "org.typelevel" %% "cats-effect" % "2.1.1",
  "co.fs2" %% "fs2-core" % "2.2.2",

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "com.softwaremill.sttp.client" %% "core" % sttpVersion,
  "com.softwaremill.sttp.client" %% "circe" % sttpVersion,
  "com.softwaremill.sttp.client" %% "async-http-client-backend-cats" % sttpVersion,

  "org.http4s" %% "http4s-core" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,

  "org.mockito" %% "mockito-scala" % mockitoVersion % Test,
  "org.mockito" %% "mockito-scala-scalatest" % mockitoVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.1" % Test,
  "com.codecommit" %% "cats-effect-testing-scalatest" % "0.4.0" % Test
)

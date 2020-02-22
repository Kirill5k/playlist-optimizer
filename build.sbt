name := "playlist-optimizer"
organization := "io.kirill"
version := "1.0"

scalaVersion := "2.13.1"

lazy val circeVersion = "0.12.3"
lazy val mockitoVersion = "1.10.3"
lazy val sttpVersion = "2.0.0-RC13"

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.12.2",
  "org.typelevel" %% "cats-core" % "2.1.0",
  "org.typelevel" %% "cats-effect" % "2.1.1",
  "co.fs2" %% "fs2-core" % "2.2.2",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.softwaremill.sttp.client" %% "core" % sttpVersion,
  "com.softwaremill.sttp.client" %% "circe" % sttpVersion,
  "com.softwaremill.sttp.client" %% "async-http-client-backend-cats" % sttpVersion,

  "org.mockito" %% "mockito-scala" % mockitoVersion % Test,
  "org.mockito" %% "mockito-scala-scalatest" % mockitoVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.1" % Test
)

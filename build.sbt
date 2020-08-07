ThisBuild / name := "playlist-optimizer"
ThisBuild / organization := "io.kirill"
ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.3"

lazy val root = (project in file("."))
  .settings(
    name := "playlist-optimizer"
  )
  .aggregate(core)

lazy val core = (project in file("core"))
  .settings(
    name := "playlist-optimizer-core",
    moduleName := "playlist-optimizer-core",
    libraryDependencies ++= Dependencies.core ++ Dependencies.test
  )

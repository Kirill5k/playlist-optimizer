ThisBuild / name := "playlist-optimizer"
ThisBuild / organization := "io.kirill"
ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.3"

lazy val Benchmark = config("benchmark") extend Test

lazy val root = (project in file("."))
  .settings(
    name := "playlist-optimizer"
  )
  .aggregate(core, frontend)

lazy val core = (project in file("core"))
  .configs(Benchmark)
  .settings(inConfig(Benchmark)(Defaults.testSettings): _*)
  .settings(
    name := "playlist-optimizer-core",
    moduleName := "playlist-optimizer-core",
    libraryDependencies ++= Dependencies.core ++ Dependencies.test,
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    logBuffered := false,
    Benchmark / parallelExecution := false,
    assembly / mainClass := Some("io.kirill.playlistoptimizer.core.Application"),
    assembly / assemblyJarName := "playlist-optimizer.jar",
    assembly / test := {},
    assembly / assemblyMergeStrategy := {
      case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
      case "application.conf"                            => MergeStrategy.concat
      case "unwanted.txt"                                => MergeStrategy.discard
      case PathList("META-INF", "MANIFEST.MF")           => MergeStrategy.discard
      case _                                             => MergeStrategy.first
    }
  )

lazy val frontend = (project in file("frontend"))
  .settings(
    name := "playlist-optimizer-frontend",
    moduleName := "playlist-optimizer-frontend"
  )

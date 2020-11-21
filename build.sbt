import com.typesafe.sbt.packager.docker._

ThisBuild / organization := "io.github.kirill5k"
ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.3"

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  publish / skip := true
)

lazy val docker = Seq(
  packageName := moduleName.value,
  version := version.value,
  maintainer := "immotional@aol.com",
  dockerBaseImage := "adoptopenjdk/openjdk15-openj9:debianslim-jre",
  dockerUpdateLatest := true,
  makeBatScripts := List()
)

lazy val Benchmark = config("benchmark") extend Test

lazy val root = (project in file("."))
  .settings(noPublish)
  .settings(
    name := "playlist-optimizer"
  )
  .aggregate(core, frontend)

lazy val core = (project in file("core"))
  .enablePlugins(JavaAppPackaging, JavaAgent, DockerPlugin)
  .configs(Benchmark)
  .settings(docker)
  .settings(inConfig(Benchmark)(Defaults.testSettings): _*)
  .settings(
    name := "playlist-optimizer-core",
    moduleName := "playlist-optimizer-core",
    libraryDependencies ++= Dependencies.core ++ Dependencies.test,
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    logBuffered := false,
    Benchmark / parallelExecution := false,
    dockerCommands := {
      val commands         = dockerCommands.value
      val (stage0, stage1) = commands.span(_ != DockerStageBreak)
      val (before, after) = stage1.splitAt(5)

      stage0 ++ before ++ Seq(Cmd("WORKDIR", "/"), Cmd("COPY", "frontend/", "/static/")) ++ after
    }
  )

lazy val frontend = (project in file("frontend"))
  .settings(noPublish)
  .settings(
    name := "playlist-optimizer-frontend",
    moduleName := "playlist-optimizer-frontend"
  )

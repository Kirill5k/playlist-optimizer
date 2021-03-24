import com.typesafe.sbt.packager.docker._

ThisBuild / organization := "io.github.kirill5k"
ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.5"

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
  dockerBaseImage := "adoptopenjdk/openjdk16-openj9:x86_64-alpine-jre-16_36_openj9-0.25.0",
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
      val copyFrontend = Seq(
        Cmd("WORKDIR", "/"),
        Cmd("RUN", "mkdir", "-p", "/static"),
        Cmd("COPY", "frontend/", "/static/")
      )

      stage0 ++ before ++ copyFrontend ++ after
    }
  )

lazy val frontend = (project in file("frontend"))
  .settings(noPublish)
  .settings(
    name := "playlist-optimizer-frontend",
    moduleName := "playlist-optimizer-frontend"
  )

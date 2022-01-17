import com.typesafe.sbt.packager.docker._

ThisBuild / organization := "io.github.kirill5k"
ThisBuild / version := scala.sys.process.Process("git rev-parse HEAD").!!.trim.slice(0, 7)
ThisBuild / scalaVersion := "3.1.0"
ThisBuild / githubWorkflowPublishTargetBranches := Nil
ThisBuild / githubWorkflowJavaVersions          := Seq("amazon-corretto@1.17")

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
  dockerBaseImage := "amazoncorretto:17.0.1-alpine",
  dockerUpdateLatest := true,
  Docker / maintainer := "kirill5k",
  dockerRepository := Some("us.gcr.io"),
  makeBatScripts := Nil,
  dockerCommands := {
    val commands         = dockerCommands.value
    val (stage0, stage1) = commands.span(_ != DockerStageBreak)
    val (before, after)  = stage1.splitAt(4)
    val installBash      = Cmd("RUN", "apk update && apk upgrade && apk add bash")
    stage0 ++ before ++ List(installBash) ++ after
  }
)

lazy val root = project
  .in(file("."))
  .settings(noPublish)
  .settings(
    name := "playlist-optimizer"
  )
  .aggregate(core, frontend)

lazy val core = project
  .in(file("core"))
  .enablePlugins(JavaAppPackaging, JavaAgent, DockerPlugin)
  .settings(docker)
  .settings(
    name := "playlist-optimizer-core",
    moduleName := "playlist-optimizer-core",
    libraryDependencies ++= Dependencies.core ++ Dependencies.test,
    Docker / packageName := "playlist-optimizer/core"
  )

lazy val frontend = project
  .in(file("frontend"))
  .settings(noPublish)
  .settings(
    name := "playlist-optimizer-frontend",
    moduleName := "playlist-optimizer-frontend"
  )

lazy val benchmark = project
  .in(file("benchmark"))
  .settings(noPublish)
  .dependsOn(core)
  .settings(
    name := "playlist-optimizer-benchmark",
    moduleName := "playlist-optimizer-benchmark",
    libraryDependencies ++= Dependencies.benchmark,
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    logBuffered := false,
    Test / parallelExecution := false
  )

import com.typesafe.sbt.packager.docker._

ThisBuild / organization                        := "io.github.kirill5k"
ThisBuild / version                             := scala.sys.process.Process("git rev-parse HEAD").!!.trim.slice(0, 7)
ThisBuild / scalaVersion                        := "3.1.0"
ThisBuild / githubWorkflowPublishTargetBranches := Nil
ThisBuild / githubWorkflowJavaVersions          := Seq("amazon-corretto@1.17")

val noPublish = Seq(
  publish         := {},
  publishLocal    := {},
  publishArtifact := false,
  publish / skip  := true
)

val docker = Seq(
  packageName         := moduleName.value,
  version             := version.value,
  maintainer          := "immotional@aol.com",
  dockerBaseImage     := "amazoncorretto:17.0.1-alpine",
  dockerUpdateLatest  := true,
  Docker / maintainer := "kirill5k",
  dockerRepository    := Some("us.gcr.io"),
  makeBatScripts      := Nil,
  dockerCommands := {
    val commands         = dockerCommands.value
    val (stage0, stage1) = commands.span(_ != DockerStageBreak)
    val (before, after)  = stage1.splitAt(4)
    val installBash      = Cmd("RUN", "apk update && apk upgrade && apk add bash")
    stage0 ++ before ++ List(installBash) ++ after
  }
)

val domain = project
  .in(file("domain"))
  .settings(
    name       := "playlist-optimizer-domain",
    moduleName := "playlist-optimizer-domain",
    libraryDependencies ++= Dependencies.domain ++ Dependencies.test
  )

val algorithm = project
  .in(file("algorithm"))
  .dependsOn(domain % "compile->compile;test->test")
  .settings(
    name       := "playlist-optimizer-algorithm",
    moduleName := "playlist-optimizer-algorithm",
    libraryDependencies ++= Dependencies.algorithm ++ Dependencies.test
  )

val core = project
  .in(file("core"))
  .dependsOn(domain % "compile->compile;test->test", algorithm)
  .enablePlugins(JavaAppPackaging, JavaAgent, DockerPlugin)
  .settings(docker)
  .settings(
    name       := "playlist-optimizer-core",
    moduleName := "playlist-optimizer-core",
    libraryDependencies ++= Dependencies.core ++ Dependencies.test,
    Docker / packageName := "playlist-optimizer/core"
  )

val frontend = project
  .in(file("frontend"))
  .settings(noPublish)
  .settings(
    name       := "playlist-optimizer-frontend",
    moduleName := "playlist-optimizer-frontend"
  )

val benchmark = project
  .in(file("benchmark"))
  .dependsOn(domain, core)
  .settings(noPublish)
  .settings(
    name       := "playlist-optimizer-benchmark",
    moduleName := "playlist-optimizer-benchmark",
    libraryDependencies ++= Dependencies.benchmark,
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    logBuffered              := false,
    Test / parallelExecution := false
  )

val root = project
  .in(file("."))
  .settings(noPublish)
  .settings(
    name := "playlist-optimizer"
  )
  .aggregate(domain, core, frontend, algorithm)

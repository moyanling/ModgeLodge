name := """ModgeLodge"""

resolvers in ThisBuild ++= Seq(
  Resolver.mavenCentral,
  Resolver.sonatypeRepo("snapshots")
)

lazy val commonSettings = Seq(
  organization := "org.mo39.fmbh",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.12.7"
)

lazy val root = (project in file(".")).settings(commonSettings: _*)

///////////////////////////////////////
////////////Project Common/////////////
///////////////////////////////////////
lazy val Common =
  (project in file("Common"))
    .settings(commonSettings: _*)
    .settings(
      name := "Common",
      libraryDependencies ++= Seq(
        "org.scala-lang.modules" %% "scala-xml" % "1.1.1" // Scala XML module
      )
    )

///////////////////////////////////////
////////////Project Mnist//////////////
///////////////////////////////////////
lazy val Mnist =
  (project in file("Mnist"))
    .dependsOn(Common)
    .settings(commonSettings: _*)
    .settings(
      name := "Mnist",
      libraryDependencies ++= Seq(
        "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-beta3", // DeepLearning for Java
        "org.nd4j" % "nd4j-native-platform" % "1.0.0-beta3", // N-Dimensional Array support for DeepLearning backend
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0", // Logging
        "ch.qos.logback" % "logback-classic" % "1.2.3" // Logging backend
      )
    )

///////////////////////////////////////
////////////////Tasks//////////////////
///////////////////////////////////////
lazy val Docker = config("docker") describedAs "docker related tasks"
lazy val build = taskKey[Unit]("Build the docker image for ModgeLodge")
build in Docker := {
  import sys.process._
  import java.nio.file.Path
  import java.nio.file.Paths
  /* Publish local Ivy repo for ModgeLodge */
  publishLocal.value
  /* Logging */
  val log = streams.value.log
  /* Handle Windows path */
  val _f = (p: Path) => p.toString.replaceAll("\\\\", "/")
  /* pwd, name and version */
  val (pwd, n, v) = (System.getProperty("user.dir"), name.value.toLowerCase, version.value)
  /* Notebook path */
  val notebook = _f(Paths.get(pwd, "notebooks"))
  /* User local Ivy repo path */
  val userRepo = _f(Paths.get(System.getProperty("user.home"), ".ivy2"))
  /* Docker build command */
  val cmd = s"docker build -t $n:$v $pwd"
  /* Execute */
  log.info(s"""Running "$cmd"""")
  cmd.! match {
    case 0 =>
      log.success("Successfully build docker image")
      log.info("Run below command to start. It would ask to share some folders for the first time.")
      val portMapping = "-p 8888:8888"
      val mountNotebook = s"-v $notebook:/home/jovyan/work"
      val mountUserRepo = s"-v $userRepo:/home/jovyan/.ivy2"
      log.info(s"docker run $portMapping $mountNotebook $mountUserRepo $n:$v")
    case _ => throw new Error("None zero exit code.")
  }
}
/* Clean up docker containers and images */
clean in Docker := {
  import sys.process._
  import scala.util.{ Failure, Success, Try }
  val log = streams.value.log
  /* Run a tuple of command and log a message if success */
  def run(t: (String, String), msg: String): Unit = {
    val (cmd1, cmd2) = t
    val r = cmd1.!! // Result of the first command
    if (r.nonEmpty) { // Might have no result
      Try(s"$cmd2 $r".!!) match {
        case Success(_) => log.success(msg)
        case Failure(e) => log.error(e.getMessage)
      }
    }
  }
  run("docker ps -q" -> "docker kill", "Stopped running containers")
  run("docker ps -aq" -> "docker rm", "Removed all containers")
  run("docker images -q --filter \"dangling=true\"" -> "docker rmi", "Removed all untagged images")
}

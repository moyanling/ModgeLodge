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

lazy val root =
  (project in file("."))
    .settings(commonSettings: _*)
    .aggregate(Mnist, Common)
    .settings(
      libraryDependencies ++= Seq(
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0", // Logging
        "ch.qos.logback" % "logback-classic" % "1.2.3", // Logging backend
        "com.github.scopt" %% "scopt" % "3.7.0"
      )
    )

///////////////////////////////////////
////////////Project Common/////////////
///////////////////////////////////////
lazy val Common =
  (project in file("Common"))
    .settings(commonSettings: _*)
    .settings(
      name := "Common",
      libraryDependencies ++= Seq(
        "org.scala-lang.modules" %% "scala-xml" % "1.1.1"
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
lazy val buildImage = taskKey[Unit]("Build the docker image for ModgeLodge")

buildImage := {
  import sys.process._
  val log = streams.value.log
  val (pwd, n, v) = ("pwd".!!.trim, name.value.toLowerCase, version.value)
  val cmd = s"docker build -t $n:$v ."
  log.info(s"""Running "$cmd"""")
  cmd.! match {
    case 0 =>
      log.success("Successfully build docker image")
      log.info("Run below command to start")
      log.info(s"docker run -p 8888:8888 -v $pwd:/home/jovyan/ $n:$v")
    case _ => throw new Error("None zero exit code.")
  }
}

lazy val cleanUp =
  taskKey[Unit]("Clean up. Stop and remove all containers. Remove all untagged images.")

cleanUp := {
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

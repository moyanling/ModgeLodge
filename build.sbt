name := """ModgeLodge"""

resolvers in ThisBuild ++= Seq(
  Resolver.mavenCentral,
  Resolver.sonatypeRepo("snapshots")
)

lazy val commonSettings = Seq(
  organization := "org.mo39.fmbh",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.11.8"
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .dependsOn(Common)
  .aggregate(Common, Mnist)

///////////////////////////////////////
/////////// Project Common ////////////
///////////////////////////////////////
lazy val Common =
  (project in file("Common"))
    .settings(commonSettings: _*)
    .settings(
      name := "Common",
      libraryDependencies ++= Seq(
        "org.scala-lang.modules" %% "scala-xml" % "1.1.1", // Scala XML module
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0", // Logging
        "ch.qos.logback" % "logback-classic" % "1.2.3" // Logging backend
      )
    )

///////////////////////////////////////
/////////// Project Mnist /////////////
///////////////////////////////////////
lazy val Mnist =
  (project in file("Mnist"))
    .dependsOn(Common)
    .aggregate(Common)
    .settings(commonSettings: _*)
    .settings(
      name := "Mnist",
      libraryDependencies ++= Seq(
        "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-beta3", // DeepLearning for Java
        "org.nd4j" % "nd4j-native-platform" % "1.0.0-beta3" // N-Dimensional Array support for DeepLearning backend
      )
    )

///////////////////////////////////////
///////////// Zeppelin ////////////////
///////////////////////////////////////
import java.nio.file.{Path, Paths}

import scala.sys.process._
import scala.util.{Failure, Success, Try}
/* Handle Windows path */
val _f = (p: Path) => p.toString.replaceAll("\\\\", "/")
/* Current working directory */
val pwd: Path = Paths.get(System.getProperty("user.dir"))
/* User home directory */
val userHome: Path = Paths.get(System.getProperty("user.home"))
/* Logs path */
val models: Path = pwd.resolve("logs")
/* Notebook path */
val notebook: Path = pwd.resolve("notebook")
/* User local maven repo path */
val userMaven: Path = userHome.resolve(".m2").resolve("repository")
val image = "apache/zeppelin"
val tag = "0.8.0" // Zeppelin does not have latest tag and 0.8.1 does not work
val portMapping = "-p 8080:8080"
val mountLogs = s"-v ${_f(models)}:/zeppelin/logs"
val mountMaven = s"-v ${_f(userMaven)}:/zeppelin/local-repo"
val mountNotebook = s"-v ${_f(notebook)}:/zeppelin/notebook"
val runZeppelin = s"docker run -d $portMapping $mountLogs $mountMaven $mountNotebook $image:$tag"
lazy val Zeppelin = config("zeppelin") describedAs "Zeppelin related tasks"
inConfig(Zeppelin) {
  /* Tasks definitions */
  Seq(
    /* Run docker container */
    run in Zeppelin := {
      description := "Run the Zeppelin docker container for ModgeLodge"
      publishM2.value
      val log = streams.value.log
      log.info(runZeppelin)
      runZeppelin.!!(log)
      log.success("Zeppelin started at http://localhost:8080")
    },
     /* Clean up docker containers and images */
     clean in Zeppelin := {
       description := "Clean up. Stop running containers, remove all containers and remove all untagged images."
       val log = streams.value.log
       /* Run a tuple of command and log a message if success. */
       def _run(cmd1: String, cmd2: String, msg: String): Unit = {
         val result = cmd1.!!
         /* Check if the result of the first command is empty */
         if (result.nonEmpty) {
           Try(s"$cmd2 $result".!!) match {
             case Success(_) => log.success(msg)
             case Failure(e) => log.error(e.getMessage)
           }
         }
       }
       _run("docker ps -q", "docker kill", "Stopped running containers")
       _run("docker ps -aq", "docker rm", "Removed all containers")
       _run("docker images -q --filter \"dangling=true\"", "docker rmi",
             "Removed all untagged images")
     }
  )
}

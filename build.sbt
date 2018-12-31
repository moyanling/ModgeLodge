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

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .dependsOn(Common)
  .aggregate(Common)

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
        "ch.qos.logback" % "logback-classic" % "1.2.3", // Logging backend
        "sh.almond" % "scala-kernel-api_2.12.7" % "0.1.12", // Integration with Scala Kernel API
//        "com.twelvemonkeys.imageio" % "imageio-core" % "3.3.2",
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
/////////////// Tasks /////////////////
///////////////////////////////////////
lazy val DockerConfig = config("docker") describedAs "docker related tasks"
lazy val build = taskKey[Unit]("Build the docker image for ModgeLodge")
inConfig(DockerConfig) {
  import sys.process._
  import java.nio.file.Path
  import java.nio.file.Paths
  import scala.util.{ Failure, Success, Try }
  /* Handle Windows path */
  val _f = (p: Path) => p.toString.replaceAll("\\\\", "/")
  /* Current working directory */
  val pwd = System.getProperty("user.dir")
  /* Notebook path */
  val notebook = _f(Paths.get(pwd, "notebooks"))
  /* User local Ivy repo path */
  val userRepo = _f(Paths.get(System.getProperty("user.home"), ".ivy2"))
  /* Command to run the docker image. Maps the port, mounts the notebooks and Ivy repo */
  val portMapping = "-p 8888:8888"
  val mountNotebook = s"-v $notebook:/home/jovyan/work"
  val mountUserRepo = s"-v $userRepo:/home/jovyan/.ivy2"
  /* Tasks definitions */
  Seq(
    /* Build docker image */
    build in DockerConfig := {
      publishLocal.value // Publish local Ivy repo for ModgeLodge
      val log = streams.value.log
      val (n, v) = (name.value.toLowerCase, version.value)
      val dockerBuildCmd = s"docker build -t $n:$v $pwd" // Docker build command
      log.info(s"""Running "$dockerBuildCmd"""")
      dockerBuildCmd.! match {
        case 0 =>
          log.success(
            s"Successfully build docker image. Run `sbt docker:run` to start the container."
          )
        case _ => throw new Error("None zero exit code.")
      }
    },
    /* Run docker container */
    run in DockerConfig := {
      description := "Run the docker container for ModgeLodge"
      val log = streams.value.log
      val (n, v) = (name.value.toLowerCase, version.value)
      val dockerRunCmd = s"docker run $portMapping $mountNotebook $mountUserRepo $n:$v"
      log.info(s"""Running "$dockerRunCmd"""")
      Try(dockerRunCmd.!!) match {
        case Failure(e) =>
          log.error("Try `sbt docker:clean`, restart your docker and run it again.")
          log.error(e.getMessage)
        case Success(_) =>
      }
    },
    /* Clean up docker containers and images */
    clean in DockerConfig := {
      description := "Clean up. Stop running containers, remove all containers and remove all untagged images."
      val log = streams.value.log
      /* Run a tuple of command and log a message if success. */
      def _run(t: (String, String), msg: String): Unit = {
        val (cmd1, cmd2) = t
        val result = cmd1.!!
        /* Check if the result of the first command is empty */
        if (result.nonEmpty) {
          Try(s"$cmd2 $result".!!) match {
            case Success(_) => log.success(msg)
            case Failure(e) => log.error(e.getMessage)
          }
        }
      }
      _run("docker ps -q" -> "docker kill", "Stopped running containers")
      _run("docker ps -aq" -> "docker rm", "Removed all containers")
      _run("docker images -q --filter \"dangling=true\"" -> "docker rmi",
           "Removed all untagged images")
    }
  )
}

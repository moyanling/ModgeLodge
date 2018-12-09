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
        "ch.qos.logback" % "logback-classic" % "1.2.3" // Logging backend
      )
    )

/* Docker image for Jupyter Notebook Env */
enablePlugins(DockerPlugin)
mainClass in Compile := Some("org.mo39.fmbh.ModgeLodge")

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

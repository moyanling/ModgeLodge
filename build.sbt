name := """ModgeLodge"""

resolvers in ThisBuild ++= Seq(
  Resolver.mavenCentral,
  Resolver.sonatypeRepo("snapshots")
)

lazy val commonSettings = Seq(
  organization := "org.mo39.fmbh",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.8"
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .enablePlugins(PlayScala)
  .aggregate(MNIST)
  .dependsOn(MNIST)

///////////////////////////////////////
////////////Project Common/////////////
///////////////////////////////////////
lazy val common = (project in file("common"))
  .settings(commonSettings)
  .settings(
    name := "modgelodge-common",
    libraryDependencies ++= Seq(
    )
  )

///////////////////////////////////////
////////////Project MNIST//////////////
///////////////////////////////////////
lazy val MNIST = (project in file("Mnist"))
  .dependsOn(common)
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

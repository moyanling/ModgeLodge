name := "ModgeLodge"

lazy val commonSettings = Seq(
  organization := "org.mo39.fmbh",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.8"
)

resolvers in ThisBuild ++= Seq(
  Resolver.mavenCentral,
  Resolver.sonatypeRepo("snapshots")
)

lazy val MNIST = (project in file("MNIST"))
  .settings(commonSettings: _*)
  .settings(
    name := "MNIST",
    libraryDependencies ++= Seq(
      "org.nd4j" % "nd4j-native-platform" % "1.0.0-beta3",
      "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-beta3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  )

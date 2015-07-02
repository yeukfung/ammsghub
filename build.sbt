name := """ammsghub"""

version := "1.0-SNAPSHOT"

lazy val libcore = (project in file("./modules/play2-lib-core")).enablePlugins(PlayScala)
lazy val libjscrud = (project in file("./modules/play2-jscrud")).enablePlugins(PlayScala)

lazy val root = (project in file(".")).enablePlugins(PlayScala).dependsOn(libcore, libjscrud)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "play-autosource"   %% "reactivemongo"       % "2.1-SNAPSHOT",
  "org.webjars.bower" % "jquery" % "1.11.0",
  "org.webjars.bower" % "angular" % "1.2.28",
  "org.webjars.bower" % "angular-resource" % "1.2.28",
  ws
)

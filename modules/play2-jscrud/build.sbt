import play.PlayImport.PlayKeys._

organization  := """net.amoeba"""
 
name := """play2-jscrud"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

lazy val njRepo = Seq(
    "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/",
    "Mandubian repository releases" at "https://github.com/mandubian/mandubian-mvn/raw/master/releases/",
    "Mandubian bintray repository releases" at "http://dl.bintray.com/mandubian/maven",
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    "sonarepo release" at "https://oss.sonatype.org/content/repositories/releases/")

resolvers in ThisBuild ++= njRepo

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)

libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23"

libraryDependencies += "org.coursera" %% "autoschema" % "0.1"

packagedArtifacts += ((artifact in playPackageAssets).value -> playPackageAssets.value)
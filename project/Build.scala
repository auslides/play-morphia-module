import sbt._
import Keys._
import play.sbt.PlayImport._
import PlayKeys._
import play.sbt.PlayJava

object PlayMorphiaModuleBuild extends Build {

  import Resolvers._
  import Dependencies._
  import BuildSettings._

  lazy val playMorphiaModdule = Project("play-morphia-module", file(".")).enablePlugins(PlayJava).settings(buildSettings).settings(
    libraryDependencies ++= runtime ++ test,

    // publishing
    //you should never set crossVersions to false on publicly published Scala artifacts.
    crossPaths := true,
	   
    publishMavenStyle := true,
	
    publishTo := {
        if (version.value.trim.endsWith("SNAPSHOT"))
	     Some(Resolver.file("file",  new File("C:\\mvn-repo\\snapshots")))
       else
	     Some(Resolver.file("file",  new File("C:\\mvn-repo\\releases")))
    },

	  
      scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-encoding", "utf8"),
      javacOptions ++= Seq("-source", "1.8", "-encoding", "utf8"),
      unmanagedResourceDirectories in Compile <+= baseDirectory( _ / "conf" ),
      resolvers ++= Seq(DefaultMavenRepository, Resolvers.typesafeRepository),
      checksums := Nil
  )

  object Resolvers {
    val typesafeRepository = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  }

  object Dependencies {
    val runtime = Seq(
      javaWs,
      "org.mongodb.morphia" % "morphia" % "1.3.0-rc0",
      "org.mongodb.morphia" % "morphia-validation" % "1.3.0-rc0",
      "org.mongodb.morphia" % "morphia-logging-slf4j" % "1.3.0-rc0",
	    "org.mongodb.morphia" % "morphia-entityscanner-plug" % "1.3.0-rc0",
      "com.blogspot.mydailyjava" % "weak-lock-free" % "0.11"
    )
	
    val test = Seq(
      "junit" % "junit" % "4.12" % "test",
      "org.easytesting" % "fest-assert" % "1.4" % "test"
    )
  }

  object BuildSettings {
    val buildOrganization = "org.auslides"
    val buildVersion = "2.5.10"
    val buildScalaVersion = "2.11.8"
    val crossBuildVersions = Seq("2.11.8")
    val buildSettings = Defaults.defaultSettings ++ Seq(
      organization := buildOrganization,
      version := buildVersion,
	  scalaVersion := buildScalaVersion,
	  crossScalaVersions := crossBuildVersions
    )
  }
}
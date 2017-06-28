import sbt._
import Keys._
import play.sbt.PlayImport._
import PlayKeys._
import play.sbt.PlayJava

object PlayMorphiaModuleBuild extends Build {

  import Resolvers._
  import Dependencies._
  import BuildSettings._

  val versionOfScala = "2.12.2"
  
  val morphiaVersion = "1.3.2"
  
  val playVersion = "2.6.0"
  
  lazy val playMorphiaModdule = Project("play-morphia-module", file(".")).enablePlugins(PlayJava).settings(buildSettings).settings(
    libraryDependencies ++= runtime ++ test,

    // publishing
    //you should never set crossVersions to false on publicly published Scala artifacts.
    crossPaths := true,
	   
    publishMavenStyle := true,
	
    publishTo := {
        if (version.value.trim.endsWith("SNAPSHOT"))
	     Some(Resolver.file("file",  new File("/var/repository/maven/snapshots")))
       else
	     Some(Resolver.file("file",  new File("/var/repository/maven/releases")))
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
      "org.mongodb.morphia" % "morphia" % morphiaVersion,
      "org.mongodb.morphia" % "morphia-validation" % morphiaVersion,
      "org.mongodb.morphia" % "morphia-logging-slf4j" % morphiaVersion,
	    "org.mongodb.morphia" % "morphia-entityscanner-plug" % morphiaVersion,
      //"javax.validation" % "validation-api" % "1.1.0.Final", // required by morphia-validation
      "javax.el" % "javax.el-api" % "2.2.5", // required by morphia-validation
      "org.glassfish.web" % "javax.el" % "2.2.5", // required by morphia-validation
      "com.blogspot.mydailyjava" % "weak-lock-free" % "0.12"
    )
	
    val test = Seq(
      "junit" % "junit" % "4.12" % "test",
      "org.easytesting" % "fest-assert" % "1.4" % "test"
    )
  }

  object BuildSettings {
    val buildOrganization = "org.auslides"
    val buildVersion = playVersion
    val buildScalaVersion = versionOfScala
    val crossBuildVersions = Seq(versionOfScala)
    val buildSettings = Defaults.defaultSettings ++ Seq(
      organization := buildOrganization,
      version := buildVersion,
	  scalaVersion := buildScalaVersion,
	  crossScalaVersions := crossBuildVersions
    )
  }
}

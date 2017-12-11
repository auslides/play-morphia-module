  import sbt._
  import Keys._
  
  object Resolvers {
    val typesafeRepository = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  }

  object Dependencies {
  
  val morphiaVersion = "1.3.2"
  
    val runtime = Seq(
      "com.typesafe.play" % "play-java_2.12" % "2.6.9",
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
    val playVersion = "2.6.9"
    val versionOfScala = "2.12.4"
  
    val buildOrganization = "org.auslides"
    val buildVersion = playVersion
    val buildScalaVersion = versionOfScala
    val crossBuildVersions = Seq(versionOfScala)
    val buildSettings = Seq(
      organization := buildOrganization,
      version := buildVersion,
	  scalaVersion := buildScalaVersion,
	  crossScalaVersions := crossBuildVersions
    )
  }
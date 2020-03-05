  import sbt._
  import Keys._
  
  object Repositories {
    val typesafeRepository = "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"
  }
  
  object Constants {
    val playVersion = "2.8.1"
    val versionOfScala = "2.13.1"
    val morphiaVersion = "1.5.8"
  }
  
  object Dependencies {  
    val runtime = Seq(
      "com.typesafe.play" % "play-java_2.13" % Constants.playVersion,
      "com.typesafe.play" % "play-guice_2.13" % Constants.playVersion,
      "com.typesafe.play" % "play-logback_2.13" % Constants.playVersion,

      "dev.morphia.morphia" % "core" % Constants.morphiaVersion,
      "dev.morphia.morphia" % "validation" % Constants.morphiaVersion,
      "dev.morphia.morphia" % "logging-slf4j" % Constants.morphiaVersion,
      "dev.morphia.morphia" % "entityscanner-plug" % Constants.morphiaVersion,
      "dev.morphia.morphia" % "guice-plug" % Constants.morphiaVersion,

      "org.hibernate" % "hibernate-validator" % "6.1.0.Final", // required by morphia-validation
      "javax.el" % "javax.el-api" % "3.0.1-b06", // required by morphia-validation
      "org.glassfish" % "javax.el" % "3.0.1-b11", // // required by morphia-validation
      "com.blogspot.mydailyjava" % "weak-lock-free" % "0.15"
    )
	
    val test = Seq(
      "junit" % "junit" % "4.12" % "test",
      "org.easytesting" % "fest-assert" % "1.4" % "test",
      "com.typesafe.play" % "play-test_2.13" % Constants.playVersion % "test"
    )
  }

  object BuildSettings {  
    val buildOrganization = "org.auslides"
    val buildVersion = Constants.playVersion
    val buildScalaVersion = Constants.versionOfScala
    val crossBuildVersions = Seq(Constants.versionOfScala)
    val buildSettings = Seq(
      organization := buildOrganization,
      version := buildVersion,
	  scalaVersion := buildScalaVersion,
	  crossScalaVersions := crossBuildVersions,
      javacOptions in (Compile,compile) ++= Seq("-source", "8", "-target", "8", "-encoding", "UTF-8")
    )
  }
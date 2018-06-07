  import sbt._
  import Keys._
  
  object Resolvers {
    val typesafeRepository = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  }
  
  object Constants {
    val playVersion = "2.6.15"
    val versionOfScala = "2.12.6"
    val morphiaVersion = "1.3.2"
  }
  
  object Dependencies {  
    val runtime = Seq(
      "com.typesafe.play" % "play-java_2.12" % Constants.playVersion,
      "org.mongodb.morphia" % "morphia" % Constants.morphiaVersion,
      "org.mongodb.morphia" % "morphia-validation" % Constants.morphiaVersion,
      "org.mongodb.morphia" % "morphia-logging-slf4j" % Constants.morphiaVersion,
      "org.mongodb.morphia" % "morphia-entityscanner-plug" % Constants.morphiaVersion,
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
import sbt._
import Keys._
  
//object PlayMorphiaModuleBuild extends Build {

  import Resolvers._
  import Dependencies._
  import BuildSettings._
  
  lazy val playMorphiaModdule = Project("play-morphia-module", file("."))
   .settings(buildSettings).settings(
    libraryDependencies ++= runtime ++ Dependencies.test,

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
      //unmanagedResourceDirectories in Compile += baseDirectory.value / "conf",
      resolvers ++= Seq(DefaultMavenRepository, Resolvers.typesafeRepository),
      checksums := Nil
  )


//}

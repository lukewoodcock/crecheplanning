name := "Crocos"
 
version := "1.0" 
      
lazy val `crocos` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.5"
//libraryDependencies += "joda-time" %% "joda-time" % "2.10.5"

      
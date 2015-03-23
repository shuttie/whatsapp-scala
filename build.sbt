name := "whatsapp-scala"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.9" withSources()

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.3.9" withSources()

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.3.9" withSources()

libraryDependencies += "commons-codec" % "commons-codec" % "1.10"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
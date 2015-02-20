name := "SdpFileProject"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3"

libraryDependencies += "org.scalaj" % "scalaj-time_2.9.1" % "0.7"

libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.5",  "org.slf4j" % "slf4j-simple" % "1.7.5")

libraryDependencies += "commons-io" % "commons-io" % "2.3"

libraryDependencies ++= Seq("com.amazonaws" % "aws-java-sdk-s3" % "1.9.19")

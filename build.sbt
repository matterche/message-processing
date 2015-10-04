name := "message-processing"

description := "Scala application that reads messages from API"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += Resolver.bintrayRepo("commercetools", "maven")

scalacOptions ++= Seq("-deprecation", "-unchecked")

testOptions in Test += Tests.Argument("-oSD")

libraryDependencies ++=
  "org.mockito" % "mockito-core" % "1.10.19" ::
    "org.scalatest" %% "scalatest" % "2.2.4" ::
    Nil map (_ % "test")

libraryDependencies ++=
  "io.sphere" %% "sphere-json" % "0.5.26" ::
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.2" ::
    "com.typesafe" % "config" % "1.2.1" ::
    "ch.qos.logback" % "logback-classic" % "1.1.2" ::
    Nil

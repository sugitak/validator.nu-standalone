import sbt._
import sbt.Keys._
// import com.github.siasia._
// import WebPlugin._
// import PluginKeys._
import sbtassembly.Plugin._
import AssemblyKeys._

/**
 * build configuration for CSS Validator Standalone
 */
object HTMLValidator extends Build {

  lazy val htmlValidator = Project(
    id = "nu-validator-standalone",
    base = file("."),

    settings = Defaults.defaultSettings /*++ webSettings*/ ++ assemblySettings ++ Seq(
      organization := "org.w3",
      version := "1.0-SNAPSHOT",
      scalaVersion := "2.9.2",
      crossScalaVersions := Seq("2.9.2"),
      javacOptions ++= Seq("-Xlint:unchecked -Xmx256m -XX:ThreadStackSize=2048"),
      mainClass in assembly := Some("org.w3.htmlvalidator.HTMLValidatorMain"),
      jarName in assembly := "validator-nu-standalone.jar",

      mergeStrategy in assembly := { 
        case "META-INF/MANIFEST.MF" => MergeStrategy.rename
        case _ => MergeStrategy.concat 
      }, 
      
      //      test in assembly := {},
      licenses := Seq("W3C License" -> url("http://opensource.org/licenses/W3C")),
      homepage := Some(url("https://github.com/w3c/validator.nu-standalone")),
      publishTo <<= version { (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
      },
      publishArtifact in Test := false,
      pomIncludeRepository := { _ => false },
      pomExtra := (
        <scm>
          <url>git@github.com:w3c/validator.nu-standalone.git</url>
          <connection>scm:git:git@github.com:w3c/validator.nu-standalone.git</connection>
        </scm>
        <developers>
          <developer>
            <id>nunnun</id>
            <name>Hirotaka Nakajima</name>
            <url>http://hirotaka.org</url>
          </developer>
        </developers>),

      resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      resolvers += "apache-repo-releases" at "http://repository.apache.org/content/repositories/releases/",
      libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.26" % "compile",
      libraryDependencies += "org.mortbay.jetty" % "servlet-api" % "2.5-20081211" % "compile",
      libraryDependencies += "org.mortbay.jetty" % "jetty-util" % "6.1.26" % "compile",
      libraryDependencies += "org.mortbay.jetty" % "jetty-ajp" % "6.1.26" % "compile",
      libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.5.2" % "compile",
      libraryDependencies += "commons-codec" % "commons-codec" % "1.4",
      libraryDependencies += "commons-httpclient" % "commons-httpclient" % "3.1",
      libraryDependencies += "commons-logging" % "commons-logging" % "1.1.1",
      libraryDependencies += "commons-logging" % "commons-logging-adapters" % "1.1.1" from "http://archive.apache.org/dist/commons/logging/binaries/commons-logging-1.1.1-bin.zip",
      libraryDependencies += "commons-logging" % "commons-logging-api" % "1.1.1" from "http://archive.apache.org/dist/commons/logging/binaries/commons-logging-1.1.1-bin.zip",
      libraryDependencies += "com.hp.hpl.jena" % "iri" % "0.5",
      libraryDependencies += "commons-fileupload" % "commons-fileupload" % "1.2.1",
      libraryDependencies += "rhino" % "js" % "1.7R1",
      libraryDependencies += "xerces" % "xercesImpl" % "2.9.1",
      libraryDependencies += "net.sourceforge.jchardet" % "jchardet" % "1.0",
      libraryDependencies += "net.sourceforge.saxon" % "saxon" % "9.1.0.2" from "http://kent.dl.sourceforge.net/sourceforge/saxon/saxonb9-1-0-2j.zip",
      libraryDependencies += "junit" % "junit" % "4.4",
      libraryDependencies += "xom" % "xom" % "1.1",
      libraryDependencies += "com.sdicons.jsontools" % "jsontools-core" % "1.5",
      libraryDependencies += "com.hp.hpl.jena" % "iri" % "0.5",
      libraryDependencies += "com.ibm.icu" % "icu4j" % "4.4.2" from "http://download.icu-project.org/files/icu4j/4.4.2/icu4j-4_4_2.jar",
      libraryDependencies += "com.ibm.icu" % "icu4j-charsets" % "4.4.2" from "http://download.icu-project.org/files/icu4j/4.4.2/icu4j-charsets-4_4_2.jar",
      libraryDependencies += "antlr" % "antlr" % "validator.nu" from "http://hsivonen.iki.fi/code/antlr.jar",
      libraryDependencies += "isorelax" % "isorelax" % "20041111" from "http://surfnet.dl.sourceforge.net/sourceforge/iso-relax/isorelax.20041111.zip"))

}

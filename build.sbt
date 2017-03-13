import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

val appVersion = "0.0.1"
val appScalaVersion = "2.12.1"
val scalaJsIoVersion = "0.3.0.7"

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.8", "-unchecked", "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-g:vars")

lazy val copyJS = TaskKey[Unit]("copyJS", "Copy JavaScript files to root directory")
copyJS := {
  val out_dir = baseDirectory.value
  val web_dir = out_dir / "app" / "webapp" / "server" / "target" / "scala-2.12"
  val cli_dir = out_dir / "app" / "webapp" / "cli" / "target" / "scala-2.12"
  val worker_dir = out_dir / "app" / "webapp" / "worker" / "target" / "scala-2.12"

  val files1 = Seq("", ".map") map ("broadway-server-fastopt.js" + _) map (s => (web_dir / s, out_dir / s))
  val files2 = Seq("", ".map") map ("broadway-cli-fastopt.js" + _) map (s => (cli_dir / s, out_dir / s))
  val files3 = Seq("", ".map") map ("broadway-worker-fastopt.js" + _) map (s => (worker_dir / s, out_dir / s))
  IO.copy(files1 ++ files2 ++ files3, overwrite = true)
}

lazy val appSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  scalaJSModuleKind := ModuleKind.CommonJSModule,
  autoCompilerPlugins := true,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/broadway.js")),
  resolvers += Resolver.sonatypeRepo("releases"),
  libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
    "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  ))

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  autoCompilerPlugins := true,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/broadway.js")),
  resolvers += Resolver.sonatypeRepo("releases"),
  libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
    "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  ))

lazy val moduleSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  scalaJSModuleKind := ModuleKind.CommonJSModule,
  autoCompilerPlugins := true,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/broadway.js")),
  resolvers += Resolver.sonatypeRepo("releases"),
  libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
    "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  ))

lazy val uiSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  persistLauncher := true,
  persistLauncher in Test := false,
  autoCompilerPlugins := true,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/broadway.js")),
  resolvers += Resolver.sonatypeRepo("releases"),
  libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
    "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  ))

lazy val webapp_common = (project in file("./app/webapp/common"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "broadway-webapp-common",
    organization := "com.github.ldaniels528.broadway",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion
    ))

lazy val webapp_rest_common = (project in file("./app/webapp/rest/common"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "broadway-rest-common",
    organization := "com.github.ldaniels528.broadway",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIoVersion
    ))

lazy val webapp_cli = (project in file("./app/webapp/cli"))
  .aggregate(webapp_common, webapp_rest_common)
  .dependsOn(webapp_common, webapp_rest_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "broadway-cli",
    organization := "com.github.ldaniels528.broadway",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "request" % "2.79.0"
    ))

lazy val webapp_client = (project in file("./app/webapp/client"))
  .aggregate(webapp_common)
  .dependsOn(webapp_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(uiSettings: _*)
  .settings(
    name := "broadway-web-client",
    organization := "com.github.ldaniels528.broadway",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "dom-html" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "angular" % "1.6.3",
      "io.scalajs.npm" %%% "angular-ui-router" % "0.4.2",
      "io.scalajs.npm" %%% "angular-toaster" % "2.1.0"
    ))

lazy val webapp_server = (project in file("./app/webapp/server"))
  .aggregate(webapp_common, webapp_client, webapp_rest_common)
  .dependsOn(webapp_common, webapp_client, webapp_rest_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "broadway-server",
    organization := "com.github.ldaniels528.broadway",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "express-csv" % "0.6.0",
      "io.scalajs.npm" %%% "express-fileupload" % "0.0.7",
      "io.scalajs.npm" %%% "express-ws" % "2.0.0",
      "io.scalajs.npm" %%% "feedparser-promised" % "1.1.1",
      "io.scalajs.npm" %%% "md5" % "1.0.2",
      "io.scalajs.npm" %%% "mongodb" % "2.2.22-4",
      "io.scalajs.npm" %%% "mongoose" % "4.8.1-2",
      "io.scalajs.npm" %%% "mean-stack" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "request" % "2.79.0",
      "io.scalajs.npm" %%% "splitargs" % "0.0.7"
    ))

lazy val webapp_worker = (project in file("./app/webapp/worker"))
  .aggregate(webapp_common, webapp_rest_common)
  .dependsOn(webapp_common, webapp_rest_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "broadway-worker",
    organization := "com.github.ldaniels528.broadway",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "csvtojson" % "1.1.4-2",
      "io.scalajs.npm" %%% "express-csv" % "0.6.0",
      "io.scalajs.npm" %%% "express-fileupload" % "0.0.7",
      "io.scalajs.npm" %%% "feedparser-promised" % "1.1.1",
      "io.scalajs.npm" %%% "glob" % "7.1.1-2",
      "io.scalajs.npm" %%% "md5" % "1.0.2",
      "io.scalajs.npm" %%% "moment" % "2.17.1-1",
      "io.scalajs.npm" %%% "moment-duration-format" % "1.3.0",
      "io.scalajs.npm" %%% "mongodb" % "2.2.22-4",
      "io.scalajs.npm" %%% "mongoose" % "4.8.1-2",
      "io.scalajs.npm" %%% "mean-stack" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "splitargs" % "0.0.7",
      "io.scalajs.npm" %%% "throttle" % "1.0.3"
    ))

lazy val broadway = (project in file("."))
  .aggregate(webapp_cli, webapp_client, webapp_server, webapp_worker)
  .dependsOn(webapp_cli, webapp_client, webapp_server, webapp_worker)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "broadway.js",
    organization := "com.github.ldaniels528.broadway",
    version := appVersion,
    scalaVersion := appScalaVersion,
    relativeSourceMaps := true,
    compile in Compile <<=
      (compile in Compile) dependsOn (fastOptJS in(webapp_client, Compile)),
    ivyScala := ivyScala.value map (_.copy(overrideScalaVersion = true)),
    Seq(packageScalaJSLauncher, fastOptJS, fullOptJS) map { packageJSKey =>
      crossTarget in(webapp_client, Compile, packageJSKey) := baseDirectory.value / "public" / "javascripts"
    })

// add the alias
addCommandAlias("fastOptJSCopy", ";fastOptJS;copyJS")

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project broadway", _: State)) compose (onLoad in Global).value

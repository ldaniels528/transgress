import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

val appVersion = "0.1.0"
val appScalaVersion = "2.12.1"
val scalaJsIoVersion = "0.4.0-pre1"

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

lazy val testDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
    "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  ))

lazy val appSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  scalaJSModuleKind := ModuleKind.CommonJSModule,
  autoCompilerPlugins := true,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/broadway.js")),
  resolvers += Resolver.sonatypeRepo("releases"))

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  autoCompilerPlugins := true,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/broadway.js")),
  resolvers += Resolver.sonatypeRepo("releases"))

lazy val moduleSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  scalaJSModuleKind := ModuleKind.CommonJSModule,
  autoCompilerPlugins := true,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/broadway.js")),
  resolvers += Resolver.sonatypeRepo("releases"))

lazy val uiSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  persistLauncher := true,
  persistLauncher in Test := false,
  autoCompilerPlugins := true,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/broadway.js")),
  resolvers += Resolver.sonatypeRepo("releases"))

lazy val common = (project in file("./app/webapp/common"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-webapp-common",
    organization := "com.github.ldaniels528.broadway",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion
    ))

lazy val rest_common = (project in file("./app/webapp/rest_common"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-rest-common",
    organization := "com.github.ldaniels528.broadway",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIoVersion
    ))

lazy val cli = (project in file("./app/webapp/cli"))
  .aggregate(common, rest_common)
  .dependsOn(common, rest_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-cli",
    organization := "com.github.ldaniels528.broadway",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIoVersion
    ))

lazy val client = (project in file("./app/webapp/client"))
  .aggregate(common)
  .dependsOn(common)
  .enablePlugins(ScalaJSPlugin)
  .settings(uiSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-web-client",
    organization := "com.github.ldaniels528.broadway",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "dom-html" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "angular" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "angular-ui-router" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "angularjs-toaster" % scalaJsIoVersion
    ))

lazy val server = (project in file("./app/webapp/server"))
  .aggregate(common, client, rest_common)
  .dependsOn(common, client, rest_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-server",
    organization := "com.github.ldaniels528.broadway",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "body-parser" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "express" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "express-fileupload" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "express-ws" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "mongodb" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "splitargs" % scalaJsIoVersion
    ))

lazy val worker = (project in file("./app/webapp/worker"))
  .aggregate(common, rest_common)
  .dependsOn(common, rest_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-worker",
    organization := "com.github.ldaniels528.broadway",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "body-parser" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "csvtojson" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "express" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "glob" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "gzip-uncompressed-size" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "mkdirp" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "moment-duration-format" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "mongodb" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "throttle" % scalaJsIoVersion
    ))

lazy val broadway_js = (project in file("."))
  .aggregate(cli, client, server, worker)
  .dependsOn(cli, client, server, worker)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway.js",
    organization := "com.github.ldaniels528.broadway",
    version := appVersion,
    scalaVersion := appScalaVersion,
    relativeSourceMaps := true,
    compile in Compile <<=
      (compile in Compile) dependsOn (fastOptJS in(client, Compile)),
    ivyScala := ivyScala.value map (_.copy(overrideScalaVersion = true)),
    Seq(packageScalaJSLauncher, fastOptJS, fullOptJS) map { packageJSKey =>
      crossTarget in(client, Compile, packageJSKey) := baseDirectory.value / "public" / "javascripts"
    })

// add the alias
addCommandAlias("fastOptJSCopy", ";fastOptJS;copyJS")

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project broadway_js", _: State)) compose (onLoad in Global).value

import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

val appVersion = "0.1.0"
val appScalaVersion = "2.12.1"
val scalaJsIoVersion = "0.4.0-pre2"

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.8", "-unchecked", "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-g:vars")

lazy val copyJS = TaskKey[Unit]("copyJS", "Copy JavaScript files to root directory")
copyJS := {
  val out_dir = baseDirectory.value
  val cli_dir = out_dir / "app" / "cli" / "target" / "scala-2.12"
  val server_dir = out_dir / "app" / "server" / "target" / "scala-2.12"
  val watcher_dir = out_dir / "app" / "watcher" / "target" / "scala-2.12"
  val worker_dir = out_dir / "app" / "worker" / "target" / "scala-2.12"

  val files1 = Seq("", ".map") map ("bourne-server-fastopt.js" + _) map (s => (server_dir / s, out_dir / s))
  val files2 = Seq("", ".map") map ("bourne-cli-fastopt.js" + _) map (s => (cli_dir / s, out_dir / s))
  val files3 = Seq("", ".map") map ("bourne-watcher-fastopt.js" + _) map (s => (watcher_dir / s, out_dir / s))
  val files4 = Seq("", ".map") map ("bourne-worker-fastopt.js" + _) map (s => (worker_dir / s, out_dir / s))
  IO.copy(files1 ++ files2 ++ files3 ++ files4, overwrite = true)
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
  homepage := Some(url("https://github.com/ldaniels528/bourne.js")),
  resolvers += Resolver.sonatypeRepo("releases"))

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  autoCompilerPlugins := true,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/bourne.js")),
  resolvers += Resolver.sonatypeRepo("releases"))

lazy val moduleSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  scalaJSModuleKind := ModuleKind.CommonJSModule,
  autoCompilerPlugins := true,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/bourne.js")),
  resolvers += Resolver.sonatypeRepo("releases"))

lazy val uiSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  autoCompilerPlugins := true,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/bourne.js")),
  resolvers += Resolver.sonatypeRepo("releases"))

lazy val common = (project in file("./app/common"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "bourne-common",
    organization := "com.github.ldaniels528",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion
    ))

lazy val rest_common = (project in file("./app/rest-common"))
  .dependsOn(common, server_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "bourne-rest-common",
    organization := "com.github.ldaniels528",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIoVersion
    ))

lazy val server_common = (project in file("./app/server-common"))
  .dependsOn(common)
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "bourne-server-common",
    organization := "com.github.ldaniels528",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIoVersion
    ))

lazy val client = (project in file("./app/client"))
  .aggregate(common)
  .dependsOn(common)
  .enablePlugins(ScalaJSPlugin)
  .settings(uiSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "bourne-web-client",
    organization := "com.github.ldaniels528",
    version := appVersion,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "dom-html" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "angular" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "angular-ui-router" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "angularjs-toaster" % scalaJsIoVersion
    ))

lazy val cli = (project in file("./app/cli"))
  .aggregate(common, rest_common, server_common)
  .dependsOn(common, rest_common, server_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "bourne-cli",
    organization := "com.github.ldaniels528",
    version := appVersion,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "glob" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "otaat-repl" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIoVersion
    ))

lazy val server = (project in file("./app/server"))
  .aggregate(common, client, server_common)
  .dependsOn(common, server_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "bourne-server",
    organization := "com.github.ldaniels528",
    version := appVersion,
    scalaJSUseMainModuleInitializer := true,
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

lazy val watcher = (project in file("./app/watcher"))
  .aggregate(common, rest_common, server_common)
  .dependsOn(common, rest_common, server_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "bourne-watcher",
    organization := "com.github.ldaniels528",
    version := appVersion,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "body-parser" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "express" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "glob" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "gzip-uncompressed-size" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "ip" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "mkdirp" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "moment-duration-format" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIoVersion
    ))

lazy val worker = (project in file("./app/worker"))
  .aggregate(common, rest_common, server_common)
  .dependsOn(common, rest_common, server_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "bourne-worker",
    organization := "com.github.ldaniels528",
    version := appVersion,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIoVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "body-parser" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "csvtojson" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "express" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "glob" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "gzip-uncompressed-size" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "ip" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "mkdirp" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "moment-duration-format" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIoVersion,
      "io.scalajs.npm" %%% "throttle" % scalaJsIoVersion
    ))

lazy val bourne_js = (project in file("."))
  .aggregate(cli, client, server, watcher, worker)
  .dependsOn(cli, client, server, watcher, worker)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "bourne.js",
    organization := "com.github.ldaniels528",
    version := appVersion,
    scalaVersion := appScalaVersion,
    relativeSourceMaps := true,
    compile in Compile <<=
      (compile in Compile) dependsOn (fastOptJS in(client, Compile)),
    ivyScala := ivyScala.value map (_.copy(overrideScalaVersion = true)),
    Seq(scalaJSUseMainModuleInitializer, fastOptJS, fullOptJS) map { packageJSKey =>
      crossTarget in(client, Compile, packageJSKey) := baseDirectory.value / "public" / "javascripts"
    })

// add the alias
addCommandAlias("fastOptJSCopy", ";fastOptJS;copyJS")

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project bourne_js", _: State)) compose (onLoad in Global).value

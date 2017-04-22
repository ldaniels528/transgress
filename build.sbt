import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

val appVersion = "0.1.1"
val appScalaVersion = "2.12.1"
val scalaJsIOVersion = "0.4.0-pre5"

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.8", "-unchecked", "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-g:vars")

lazy val copyJS = TaskKey[Unit]("copyJS", "Copy JavaScript files to root directory")
copyJS := {
  val out_dir = baseDirectory.value
  val cli_dir = out_dir / "app" / "cli" / "target" / "scala-2.12"
  val supervisor_dir = out_dir / "app" / "supervisor" / "target" / "scala-2.12"
  val watcher_dir = out_dir / "app" / "watcher" / "target" / "scala-2.12"
  val worker_dir = out_dir / "app" / "worker" / "target" / "scala-2.12"

  val files1 = Seq("", ".map") map ("broadway-cli-fastopt.js" + _) map (s => (cli_dir / s, out_dir / s))
  val files2 = Seq("", ".map") map ("broadway-supervisor-fastopt.js" + _) map (s => (supervisor_dir / s, out_dir / s))
  val files3 = Seq("", ".map") map ("broadway-watcher-fastopt.js" + _) map (s => (watcher_dir / s, out_dir / s))
  val files4 = Seq("", ".map") map ("broadway-worker-fastopt.js" + _) map (s => (worker_dir / s, out_dir / s))
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
  autoCompilerPlugins := true,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/broadway.js")),
  resolvers += Resolver.sonatypeRepo("releases"))

lazy val common_core = (project in file("./app/common/core"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-common-core",
    organization := "com.github.ldaniels528",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion
    ))

lazy val common_cli = (project in file("./app/common/cli"))
  .dependsOn(common_core)
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-common-cli",
    organization := "com.github.ldaniels528",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion
    ))

lazy val common_rest = (project in file("./app/common/rest"))
  .dependsOn(common_core, common_cli)
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-common-rest",
    organization := "com.github.ldaniels528",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion
    ))

lazy val common_worker = (project in file("./app/common/worker"))
  .dependsOn(common_core)
  .settings(commonSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-common-worker",
    organization := "com.github.ldaniels528",
    version := appVersion,
    libraryDependencies ++= Seq(

    ))

lazy val client = (project in file("./app/client"))
  .aggregate(common_core)
  .dependsOn(common_core)
  .enablePlugins(ScalaJSPlugin)
  .settings(uiSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-web-client",
    organization := "com.github.ldaniels528",
    version := appVersion,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "dom-html" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "angular" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "angular-ui-router" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "angularjs-toaster" % scalaJsIOVersion
    ))

lazy val cli = (project in file("./app/cli"))
  .aggregate(common_core, common_rest, common_cli)
  .dependsOn(common_core, common_rest, common_cli)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-cli",
    organization := "com.github.ldaniels528",
    version := appVersion,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "glob" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "otaat-repl" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion
    ))

lazy val supervisor = (project in file("./app/supervisor"))
  .aggregate(common_core, client, common_cli)
  .dependsOn(common_core, common_cli)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-supervisor",
    organization := "com.github.ldaniels528",
    version := appVersion,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "body-parser" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express-fileupload" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express-ws" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mongodb" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "splitargs" % scalaJsIOVersion
    ))

lazy val watcher = (project in file("./app/watcher"))
  .aggregate(common_core, common_rest, common_cli)
  .dependsOn(common_core, common_rest, common_cli)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-watcher",
    organization := "com.github.ldaniels528",
    version := appVersion,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "aws-s3" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "body-parser" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "glob" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "gzip-uncompressed-size" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "ip" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mkdirp" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-duration-format" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion
    ))

lazy val worker_js = (project in file("./app/worker_js"))
  .aggregate(common_core, common_rest, common_cli, common_worker)
  .dependsOn(common_core, common_rest, common_cli, common_worker)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-worker-js",
    organization := "com.github.ldaniels528",
    version := appVersion,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "body-parser" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "csvtojson" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "glob" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "gzip-uncompressed-size" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "ip" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mkdirp" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-duration-format" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mongodb" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "throttle" % scalaJsIOVersion
    ))

lazy val worker_jvm = (project in file("./app/worker_jvm"))
  .aggregate(common_core, common_worker)
  .dependsOn(common_core, common_worker)
  .settings(commonSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-worker-jvm",
    organization := "com.github.ldaniels528",
    version := appVersion,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.5.0"
    ))

lazy val broadway = (project in file("."))
  .aggregate(cli, client, supervisor, watcher, worker_js)
  .dependsOn(cli, client, supervisor, watcher, worker_js)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(testDependencies: _*)
  .settings(
    name := "broadway-bundle",
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
onLoad in Global := (Command.process("project broadway", _: State)) compose (onLoad in Global).value

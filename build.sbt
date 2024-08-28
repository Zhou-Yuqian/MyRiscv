ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.14"
ThisBuild / organization := "zhou@10.212.67.7"

val SpinalVersion = "1.10.0"
val SpinalCore = "com.github.spinalhdl" %% "spinalhdl-core" % SpinalVersion
val SpinalLib = "com.github.spinalhdl" %% "spinalhdl-lib" % SpinalVersion
val SpinalIdslPlugin = compilerPlugin("com.github.spinalhdl" %% "spinalhdl-idsl-plugin" % SpinalVersion)

lazy val MyRiscv = (project in file("."))
  .settings(
    name := "MyRiscv",
    libraryDependencies ++= Seq(SpinalCore, SpinalLib, SpinalIdslPlugin)
  )

fork := true

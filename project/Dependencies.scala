import sbt._

object Dependencies {
  lazy val ZioVersion = "1.0.12"
  lazy val zio        = Seq(
    "dev.zio" %% "zio"               % ZioVersion,
    "dev.zio" %% "zio-macros"        % ZioVersion,
    "dev.zio" %% "zio-test"          % ZioVersion % Test,
    "dev.zio" %% "zio-test-sbt"      % ZioVersion % Test,
    "dev.zio" %% "zio-logging-slf4j" % "0.5.14",
    "dev.zio" %% "zio-interop-cats"  % "3.2.9.1"
  )

  lazy val Http4sVersion = "0.23.10"
  lazy val http4s        = Seq(
    "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
    "org.http4s" %% "http4s-dsl"          % Http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % Http4sVersion % Test
  )

  lazy val logback = Seq("ch.qos.logback" % "logback-classic" % "1.2.10")
}

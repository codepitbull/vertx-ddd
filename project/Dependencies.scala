import sbt._

object Version {
  final val Scala        = "2.12.1"
  final val ScalaTest    = "3.0.0"
  final val Vertx        = "3.4.1"
  final val OpenHftQueue = "4.5.23"
  final val OpenHftMap   = "3.12.0"
  final val Jackson      = "2.8.7"
  final val Mockito      = "2.7.12"
  final val RxScala      = "0.26.5"
}

object Library {
  val vertxCodegen          = "io.vertx"                       %  "vertx-codegen"        % Version.Vertx        % "provided" changing()
  val vertxLangScala        = "io.vertx"                       %% "vertx-lang-scala"     % Version.Vertx                     changing()
  val vertxHazelcast        = "io.vertx"                       %  "vertx-hazelcast"      % Version.Vertx                     changing()
  val scalaTest             = "org.scalatest"                  %% "scalatest"            % Version.ScalaTest
  val chronicleQueue        = "net.openhft"                    %  "chronicle-queue"      % Version.OpenHftQueue
  val chronicleMap          = "net.openhft"                    %  "chronicle-map"        % Version.OpenHftMap
  val jacksonScala          = "com.fasterxml.jackson.module"   %% "jackson-module-scala" % Version.Jackson
  val mockito               = "org.mockito"                    %  "mockito-core"         % Version.Mockito
  val rxScala               = "io.reactivex"                   %% "rxscala"              % Version.RxScala
  val vertxKryo             = "de.codepitbull.vertx.scala.ext" %% "kryo-codec"           % Version.Vertx
}
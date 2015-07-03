name := """twofactorauthpoc"""

version := "1.0-SNAPSHOT"

lazy val currencyFairTest = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(javacOptions := Seq(
    "-J-Xms512M",
    "-J-Xmx1024M",
    "-J-XX:MaxPermSize=1024M",
    "-J-Xss2M",
    "-J-XX:+UseConcMarkSweepGC",
    "-J-XX:+CMSClassUnloadingEnabled"))

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.jboss.aerogear" %  "aerogear-otp-java" % "1.0.0",
//  "com.warrenstrange"  %  "googleauth"        % "0.4.3",  //this library seems better build but can't use it for sms code generation
  "com.h2database"     %  "h2"                % "1.4.177",
  "com.typesafe.play"  %  "play-json_2.11"    % "2.3.7",
  "com.typesafe.play"  %% "play-ws"           % "2.3.7",
  "com.typesafe.slick" %% "slick"             % "2.1.0")


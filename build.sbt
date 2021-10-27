name := "twitter-analysis"

version := "0.1"

scalaVersion := "2.12.10"

val flinkVersion = "1.13.2"
val sparkVersion = "3.1.2"
libraryDependencies ++= Seq(
  "org.apache.flink" %% "flink-connector-twitter" % flinkVersion,
  "org.apache.flink" %% "flink-scala" % flinkVersion,
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion,
  "org.apache.flink" %% "flink-clients" % flinkVersion,
  "net.liftweb" %% "lift-json" % "3.4.3",
  "commons-logging" % "commons-logging" % "1.2",
  "org.slf4j" % "slf4j-simple" % "1.7.32",
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.3.3",
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.mongodb.spark" %% "mongo-spark-connector" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.2.10" % Test,
  "org.mockito" % "mockito-core" % "3.11.2" % Test,
  "org.mockito" %% "mockito-scala" % "1.16.46" % Test
)
package twitter

import com.typesafe.config.ConfigFactory
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import org.apache.spark.sql.functions.{col, collect_list, size}
import org.apache.spark.sql.{Encoder, Encoders, SparkSession}
import twitter.MongoHelpers._

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

object HeatMap {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("HeatMap").master("local[*]").getOrCreate()
    val tweets = MongoConnection.tweetCollection.find().results()
    val quadTreeLevel = ConfigFactory.load().getString("quadtree.level").toInt
    val numberOfNodes = Math.pow(2, quadTreeLevel)
    val width = 360.0 / numberOfNodes
    val height = 180.0 / numberOfNodes

    import spark.implicits._
    implicit val JValueEncoder: Encoder[JValue] = Encoders.kryo[JValue]
    implicit val SeqJObjectEncoder: Encoder[Seq[JObject]] = Encoders.kryo[Seq[JObject]]

    val tweetsByNode = tweets.toDS()
      .map(t => findNodeForTweet(t.place.boundingBox.coordinates.head, width, height) -> t)
      .groupBy(col("_1"))
      .agg(collect_list("_2") as "tweets")
      .cache()

    val maxNodeSize = tweetsByNode
      .sort(size(col("tweets")).desc)
      .first()
      .getAs[Seq[Tweet]]("tweets").size

    val featuresOnLevel = tweetsByNode.as[((Double, Double), Seq[Tweet])]
      .map(r => buildJson(r._1, r._2, maxNodeSize, width, height))
      .reduce(_ ++ _)

    writeToFile(prettyRender(
      ("type" -> "FeatureCollection") ~
        ("features" -> (featuresOnLevel ++ buildNodes(quadTreeLevel = quadTreeLevel)))))
  }

  def findNodeForTweet(p: Point, width: Double, height: Double): (Double, Double) =
    (((p.x + 180) / width).toInt * width - 180, ((p.y + 90) / height).toInt * height - 90)

  def buildJson(node: (Double, Double), tweets: Seq[Tweet], maxNodeSize: Int, width: Double, height: Double): Seq[JObject] = {
    val color = calculateNodeColor(tweets.size, maxNodeSize)
    buildTweetsJson(tweets) :+ buildNodeJson(node._1, node._2, width, height, s"rgb($color, $color, $color)")
  }

  def calculateNodeColor(nodeSize: Int, maxNodeSize: Int): Int = (255 - nodeSize.toDouble / maxNodeSize * 255).toInt

  def buildNodeJson(x: Double, y: Double, width: Double, height: Double, color: String): JObject =
    ("type" -> "Feature") ~
      ("properties" ->
        ("fill" -> color)) ~
      ("geometry" ->
        ("type" -> "Polygon") ~
          ("coordinates" -> List(List(List(x, y), List(x + width, y), List(x + width, y + height), List(x, y + height), List(x, y)))))

  def buildTweetsJson(tweets: Seq[Tweet]): Seq[JObject] =
    tweets.map { t =>
      ("type" -> "Feature") ~
        ("properties" ->
          ("user" -> t.name) ~
            ("time" -> t.createdAt) ~
            ("text" -> t.text)) ~
        ("geometry" ->
          ("type" -> "Point") ~
            ("coordinates" -> List(t.place.boundingBox.coordinates.head.x, t.place.boundingBox.coordinates.head.y)))
    }

  def buildNodes(x: Double = -180, y: Double = -90, width: Double = 360, height: Double = 180, level: Int = 0, quadTreeLevel: Int): Seq[JObject] = {
    if (level == quadTreeLevel - 1) Seq[JObject]()
    else {
      val halfWidth = width / 2
      val halfHeight = height / 2
      buildNodes(x, y, halfWidth, halfHeight, level + 1, quadTreeLevel) ++
        buildNodes(x + halfWidth, y, halfWidth, halfHeight, level + 1, quadTreeLevel) ++
        buildNodes(x, y + halfHeight, halfWidth, halfHeight, level + 1, quadTreeLevel) ++
        buildNodes(x + halfWidth, y + halfHeight, halfWidth, halfHeight, level + 1, quadTreeLevel) :+
        buildNodeJson(x, y, width, height, "none")
    }
  }

  def writeToFile(json: String) = Files.write(Paths.get("map.json"), json.getBytes(StandardCharsets.UTF_8))
}

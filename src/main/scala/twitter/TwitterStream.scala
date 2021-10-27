package twitter

import net.liftweb.json.{DefaultFormats, _}
import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.twitter.TwitterSource
import org.apache.flink.util.Collector
import MongoHelpers._

object TwitterStream {
  def main(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val streamSource: DataStream[String] = env.addSource(new TwitterSource(params.getProperties))

    val tweets: DataStream[Tweet] = streamSource.flatMap(new TweetParser)
    tweets.addSink(MongoConnection.tweetCollection.insertOne(_).results())

    env.execute("Twitter location processing")
  }
}

class TweetParser extends FlatMapFunction[String, Tweet] {
  override def flatMap(value: String, out: Collector[Tweet]): Unit = {
    implicit val formats = DefaultFormats
    val json = parse(value)
    val userName = json \ "user" \ "name"
    val placeType_ = (json \ "place" \ "place_type").extractOpt[String]
    if (!((userName == JNothing) || (userName == JNull)) && placeType_.contains("poi")) {
      val name = userName.extract[String]
      val createdAt = (json \ "created_at").extract[String]
      val text = (json \ "text").extract[String]
      val placeType = placeType_.get
      val boundingBoxType = (json \ "place" \ "bounding_box" \ "type").extract[String]
      val coordinates = (json \ "place" \ "bounding_box" \ "coordinates")
        .children.flatMap(_.children).map(_.children).map(l => Point(l.head.extract[Double], l.last.extract[Double]))
      out.collect(Tweet(name, createdAt, text, Place(placeType, BoundingBox(boundingBoxType, coordinates))))
    }
  }
}
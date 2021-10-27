package twitter

import org.apache.flink.util.Collector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.never
import org.mockito.MockitoSugar.{mock, verify}
import org.scalatest.flatspec.AnyFlatSpec

import scala.io.Source

class TweetParserTest extends AnyFlatSpec {
  behavior of "TweetParser"

  it should "map Tweet json with place type poi to Tweet object" in {
    val tweetParser = new TweetParser
    val out = mock[Collector[Tweet]]
    val tweetJson = Source.fromResource("flink_twitter_stream/poi_tweet.json").mkString
    val tweet = Tweet("user name", "Tue Oct 26 12:00:00 +0000 2021", "some text", Place("poi",
      BoundingBox("Polygon", List[Point](Point(1, 1), Point(1, 1), Point(1, 1), Point(1, 1)))))
    tweetParser.flatMap(tweetJson, out)
    verify(out).collect(tweet)
  }

  it should "not map Tweet json with place type other than poi to Tweet object" in {
    val tweetParser = new TweetParser
    val out = mock[Collector[Tweet]]
    val tweetJson = Source.fromResource("flink_twitter_stream/not_poi_tweet.json").mkString
    tweetParser.flatMap(tweetJson, out)
    verify(out, never()).collect(any())
  }

  it should "not map delete Tweets json" in {
    val tweetParser = new TweetParser
    val out = mock[Collector[Tweet]]
    val tweetJson = Source.fromResource("flink_twitter_stream/delete_tweet.json").mkString
    tweetParser.flatMap(tweetJson, out)
    verify(out, never()).collect(any())
  }
}

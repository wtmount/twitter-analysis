package twitter

import net.liftweb.json.JsonDSL._
import net.liftweb.json.prettyRender
import org.scalatest.flatspec.AnyFlatSpec

import scala.io.Source

class HeatMapTest extends AnyFlatSpec {
  behavior of "HeatMapTest"

  it should "build empty nodes of QuadTree up to level 3" in {
    val expectedJson = Source.fromResource("heatmap/quadtree_nodes_level3.json").mkString
    val actualJson = prettyRender(HeatMap.buildNodes(quadTreeLevel = 3))
    assert(actualJson == expectedJson)
  }

  it should "find QuadTree node for tweet on level 2" in {
    val point = Point(100, 50)
    val width = 45
    val height = 22.5
    val node = HeatMap.findNodeForTweet(point, width, height)
    assert(node == (90, 45))
  }

  it should "calculate QuadTree node color" in {
    val currentNodeSize = 2
    val maxNodeSize = 10
    val color = HeatMap.calculateNodeColor(currentNodeSize, maxNodeSize)
    assert(color == 204)
  }

  it should "build a quadtree node json" in {
    val expectedJson = Source.fromResource("heatmap/quadtree_node.json").mkString
    val actualJson = prettyRender(HeatMap.buildNodeJson(1, 1, 1, 1, "rgb(1, 1, 1)"))
    assert(actualJson == expectedJson)
  }

  it should "build a json array of tweets from Tweets" in {
    val expectedJson = Source.fromResource("heatmap/tweets.json").mkString
    val tweet1 = Tweet("user1", "Tue Oct 19 12:00:00 +0000 2021", "text1", Place("poi", BoundingBox("Polygon",
      List[Point](Point(1, 1), Point(1, 1), Point(1, 1), Point(1, 1)))))
    val tweet2 = Tweet("user2", "Tue Oct 19 12:30:00 +0000 2021", "text2", Place("poi", BoundingBox("Polygon",
      List[Point](Point(2, 2), Point(2, 2), Point(2, 2), Point(2, 2)))))
    val actualJson = prettyRender(HeatMap.buildTweetsJson(Seq(tweet1, tweet2)))
    assert(actualJson == expectedJson)
  }


  it should "build a json array of node its tweets" in {
    val expectedJson = Source.fromResource("heatmap/node_with_tweets.json").mkString
    val tweet = Tweet("user", "Tue Oct 19 12:00:00 +0000 2021", "text", Place("poi", BoundingBox("Polygon",
      List[Point](Point(1.5, 1.5), Point(1.5, 1.5), Point(1.5, 1.5), Point(1.5, 1.5)))))
    val actualJson = prettyRender(HeatMap.buildJson((1, 1), Seq(tweet), 1, 1, 1))
    assert(actualJson == expectedJson)
  }
}

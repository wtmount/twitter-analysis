package twitter

case class Tweet(name: String, createdAt: String, text: String, place: Place)
case class Place(placeType: String, boundingBox: BoundingBox)
case class BoundingBox(`type`: String, coordinates: List[Point])
case class Point(x: Double, y: Double)
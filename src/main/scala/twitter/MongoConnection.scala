package twitter

import com.typesafe.config.ConfigFactory
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala._
import org.mongodb.scala.bson.codecs.Macros._

object MongoConnection {
  private val uri = ConfigFactory.load().getString("db.host") + ":" + ConfigFactory.load().getString("db.port")
  private val mongoClient = MongoClient(uri)
  private val codecRegistry = fromRegistries(fromProviders(
    classOf[Tweet], classOf[Place], classOf[BoundingBox], classOf[Point]), DEFAULT_CODEC_REGISTRY)
  private val dbName = ConfigFactory.load().getString("db.name")
  private val db = mongoClient.getDatabase(dbName).withCodecRegistry(codecRegistry)
  val tweetCollection: MongoCollection[Tweet] = db.getCollection("tweets")
}

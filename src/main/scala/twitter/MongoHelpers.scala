package twitter

import org.mongodb.scala._

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object MongoHelpers {
  implicit class GenericObservable[C](val observable: Observable[C]) {
    val converter: (C) => String = (doc) => doc.toString
    def results(): Seq[C] = Await.result(observable.toFuture(), Duration(10, TimeUnit.SECONDS))
  }
}

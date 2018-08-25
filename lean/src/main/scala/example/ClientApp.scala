package example

import com.twitter.finagle.service.RetryBudget
import com.twitter.finagle.{Http, http}
import com.twitter.util.{Await, Duration, Future}

object ClientApp extends App {

  val budget = RetryBudget(
    ttl = Duration.fromSeconds(10),
    minRetriesPerSec = 1,
    percentCanRetry = 0.1
  )

  val request = http.Request(http.Method.Get, "/")
  request.host = "localhost"

  val client = Http.client
    .withRetryBudget(budget)
    .newService("localhost:8080")

  val response: Future[http.Response] = client(request)
    .onSuccess { rep: http.Response =>
      println("GET success: " + rep.getContentString())
    }.onFailure { th: Throwable => println("Thrown : " + th) }


  Await.result(response)
  println("YO")

}

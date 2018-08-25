package example

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await

object ServerApp extends App {

  val service: Service[Request, Response] = MinService

  val server = Http.server
    .serve(addr = ":8080", service)

  Await.ready(server)
}
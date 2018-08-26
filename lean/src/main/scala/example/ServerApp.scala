package example

import com.twitter.finagle.{Http, Service, http}
import com.twitter.util.Await

object ServerApp extends App {

  val service: Service[http.Request, http.Response] = MinService

  val server = Http.server
    .serve(addr = ":8080", service)

  Await.ready(server)
}
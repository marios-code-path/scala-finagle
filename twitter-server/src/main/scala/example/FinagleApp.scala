package example

import com.twitter.finagle.Http
import com.twitter.inject.server.TwitterServer
import com.twitter.util.Await

object FinagleAppMain extends FinagleApp

class FinagleApp extends App {
  val service = new MyService(true)
  val server = Http.serve(":8080", service)

  Await.ready(server)
}

class TwitterApp extends TwitterServer {
  override val modules = Seq(MyModule)

  override protected def start(): Unit = {
    val service = injector.instance[MyService]
    val server = Http.serve(":8080", service)
    onExit {
      server.close()
    }
  }

}
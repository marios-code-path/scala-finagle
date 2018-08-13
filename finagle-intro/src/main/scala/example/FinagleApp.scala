package example

import com.twitter.finagle.Http
import com.twitter.inject.server.TwitterServer

object FinagleAppMain extends FinagleApp

class FinagleApp extends TwitterServer {
  override val modules = Seq(MyModule)

  override protected def start(): Unit = {
    val service = injector.instance[MyService]
    val server = Http.serve(":8080", service)
    onExit {
      server.close()
    }
  }

}
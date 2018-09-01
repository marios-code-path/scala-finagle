package example

import java.net.InetSocketAddress

import com.twitter.finagle.Http
import com.twitter.util.Await

object SampleApp extends com.twitter.inject.app.App {
  val servicePort = flag("port", new InetSocketAddress(8080), "Specify TCP port to listen on")
  val flagReverse = flag("reverse", false, "Reverses the string.")

  override def run(): Unit = {
      val service = new SampleService(flagReverse())
      val server = Http.serve(servicePort(), service)

      Await.ready(server)
  }

}

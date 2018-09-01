package example

import java.net.InetSocketAddress

import com.twitter.finagle.Http


object SampleApp
  extends com.twitter.inject.app.App {
  override val modules = Seq(SampleModule)

  val servicePort = flag("port", new InetSocketAddress(8080), "Specify TCP port to listen on")
  val service = injector.instance[SampleService]

  override def main(): Unit = {
    val server = Http.serve(servicePort(), service)

    onExit {
      server.close()
    }
  }
}
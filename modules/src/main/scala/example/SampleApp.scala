package example

import java.net.InetSocketAddress

import com.twitter.finagle.Http
import com.twitter.util.Await

object SampleApp
  extends com.twitter.inject.app.App {
  override val modules = Seq(SampleModule)

  val flagServicePort = flag("port", new InetSocketAddress(8080), "Specify TCP port to listen on")

  postmain {
    val server = Http.serve(flagServicePort(), injector.instance[SampleService])

    onExit {
      server.close()
    }

    Await.ready(server)
  }
}
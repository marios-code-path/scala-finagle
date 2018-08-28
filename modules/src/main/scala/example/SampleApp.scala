package Example

import com.twitter.finagle.Http
import com.twitter.inject.server.TwitterServer
import example.{SampleModule, SampleService}

class SampleApp
  extends TwitterServer {
  override val modules = Seq(SampleModule)

  override protected def start(): Unit = {
    val service = injector.instance[SampleService]
    val server = Http.serve(":8080", service)

    onExit {
      server.close()
    }
  }
}
package example

import com.twitter.finagle.http.filter.{CommonLogFormatter, LoggingFilter}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.logging.Logger
import com.twitter.util.{Await, Duration}

object MyLoggingFilter
  extends LoggingFilter[Request]({
    val log = Logger("access")
    log.setUseParentHandlers(true)
    log
  }, new CommonLogFormatter)

object SampleApp extends App {
  val service: Service[Request, Response] = MyLoggingFilter.andThen(SampleService)

  val server = Http.server
    .withRequestTimeout(Duration.fromSeconds(30))
    .withLabel("sampleServer")
    .serve(addr = ":8080", service)

  Await.ready(server)
}
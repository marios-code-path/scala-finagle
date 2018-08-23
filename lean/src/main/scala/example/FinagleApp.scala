package example

import com.twitter.finagle.http.filter.{CommonLogFormatter, LoggingFilter}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.SummarizingStatsReceiver
import com.twitter.finagle.{Http, Service}
import com.twitter.logging.Logger
import com.twitter.util.{Await, Duration}

object SummaryStatsReceiver extends SummarizingStatsReceiver

object MyLoggingFilter
  extends LoggingFilter[Request]({
    val log = Logger("access")
    log.setUseParentHandlers(true)
    log
  }, new CommonLogFormatter)

object FinagleApp extends App {

  val service: Service[Request, Response] = new ExampleFilter(_ =>
    SummaryStatsReceiver
      .counter("filtered_requests").incr()
  )
    .andThen(MyLoggingFilter)
    .andThen(MinimumService)

  val server = Http.server
    .withRequestTimeout(Duration.fromSeconds(30))
    .withStatsReceiver(SummaryStatsReceiver)
    .withHttpStats
    .withLabel("myServer")
    .serve(addr = ":8080", service)

  sys.addShutdownHook(
    SummaryStatsReceiver.print()
  )

  Await.ready(server)
}
package example

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.SummarizingStatsReceiver
import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Duration}

object SummaryStatsReceiver extends SummarizingStatsReceiver

object FinagleApp extends App {

  val service: Service[Request, Response] = new ExampleFilter(_ =>
    SummaryStatsReceiver
      .counter("filtered_requests").incr()
  )
    .andThen(MinimumService)

  val server = Http.server
    .withRequestTimeout(Duration.fromSeconds(30))
    .withStatsReceiver(SummaryStatsReceiver)
    .withHttpStats
    .withLabel("example")
    .serve(addr = ":8080", service)

  sys.addShutdownHook(
    SummaryStatsReceiver.print()
  )

  Await.ready(server)
}
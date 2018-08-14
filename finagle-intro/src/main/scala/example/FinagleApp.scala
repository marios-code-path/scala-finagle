package example

import com.twitter.finagle.Http
import com.twitter.finagle.stats.SummarizingStatsReceiver
import com.twitter.util.{Await, Duration}

object FinagleApp extends App {

  object SummaryStatsReceiver extends SummarizingStatsReceiver

  val server = Http.server
    .withRequestTimeout(Duration.fromSeconds(30))
    .withStatsReceiver(SummaryStatsReceiver)
    .withLabel("example")
    .serve(addr = ":8080", service = new MyService(true))

  Await.ready(server)
}
+++
date = 2018-07-30
publishDate = 2018-08-04
title = "Intro to Finagle services with Scala and SBT."
description = "Finagle lets you develop and deploy services easily."
toc = true
categories = ["scala","twitter","finagle"]
tags = ["functional","scala","services","demo"]
+++
# Standing up HTTP/RPC services with Twitter/Finagle API

## Why Finagle

[Finagle](https://twitter.github.io/finagle/) is a Service building framework that lets developers write non-blocking services with ease. I put together this introduction to start of line of developing software with multiple services frameworks that also include Spring, and DropWizard. We will go through the steps to deploy a Finagle Service that takes advantage of the Twitter services eco-system in TwitterServer, Guice and of coure Scala.

### JVM Functions in Scala

Finagle is written in Scala, and works best in applications - scala or java - that want to take the [functional services](https://monkey.org/~marius/funsrv.pdf) approach. You can prefer to choose ordinary Java or some other JVM language.

### Build Dependencies

We will highlight two important building blocks for our Services to use: [Service](https://service.html), [Filters](https://twitter.github.io/ffilters) and [StatsReceivers](http://stats-receivers). To enable just these basic components, add 'finagle-http' to your build as seen below.

```c
name := "example-service"
version := "1.0"
libraryDependencies += "com.twitter" %% "finagle-http" % "18.8.0"
```

## Quick Finagle HTTP Service

Finagle lets you develop services in a conventional, programatic and functional way. You may stand up a single HTTP server by extending the base trait [Service](https://twitter.github.io/finagle/guide/ServicesAndFilters.html) and specifying your `Req` and `Res` types in generic form. To see this in action, we will demonstrate HTTP service by constructing a simple `Service[http.Request, http.Response]`.

A generic Finagle service uses the [Service](https://twitter.github.io/finagle/guide/ServicesAndFilters.html) trait to expose service functionality such that any Service[Req, Res] `A` may consume `Req`, and respond with `Res`. This can be seen in the sample code below, where we use [http.Request](http://www.github.com/) and `http.Response` as the input/output types. Because Finagle is an [RPC](http://link-to-some-rpc-doc) system, we must implement the `apply(Req): Future[Res]` methods where the response is a [Future](http://Future) of the returned `res` type `http.Response`.

```scala
import com.twitter.finagle.http.{Response, Status}
import com.twitter.finagle.{Service, http}
import com.twitter.util.Future

class MyService(showMinimum: Boolean) extends Service[http.Request, http.Response] {
  
  val seed = Seq(76, 69, 71, 48, 83, 42)

  def apply(req: http.Request): Future[http.Response] = {
    val response = Response(req.version, Status.Ok)
    val sample = if (showMinimum) seed.min else seed.max
    val string = if (showMinimum) "Minimum" else "Maximum"

    response.setContentString(s"${string} target sample is: ${sample}")

    Future.value(
      response
    )
  }

}
```

## The Finagle HTTP Server

Finagle allows us to define our [Server](http://twitter-finagle-server) through the same `Req` / `Res` input/output types we defined in our Service./
Finagle comes pre-packaged with a couple protocol-specific servers that we could use to harness our Service's functionality. We will observe Finagle [Http](http://twitter-finagle-http) server capabilities, and how to configure and start the server.

In this example, our `MyService` service responds to all HTTP URI's on our localhost Server instance. The Filters we provided ensure stats are counted towards all requests, and logs produced through the [LoggingFilter](http://logging-filter) filter.

```scala
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.SummarizingStatsReceiver
import com.twitter.util.{Await, Duration}

object SummaryStatsReceiver extends SummarizingStatsReceiver

object FinagleApp extends App {

  val service: Service[Request, Response] = new ExampleFilter(_ =>
    SummaryStatsReceiver
      .scope("example").counter("filtered_requests").incr()
  ).andThen(new MyService(true))

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
```

### Statistics Gathering

A [StatsReceiver](http://stats-receivers) is useful for tracking performance and usage of our App. This example uses [SummarizingStatsReciever](http://summarizing-stats) to track a variety of statistics accross our service stack. In this example, we wired a stats receiver through The Server instance, and turned HTTP statistics gathering via the `withHttpStats` toggle. This configuration will enable the collection of multiple HTTP metrics.

Serving to individualize our Service among other service statistics is the `withLabel()` method. This simply labels the root prefix for our particular service, such that it can be identified when reading statistics of a (for example) multi-tenant Server.

We needed to provide a way for SummariziginStatsReceier to output it's report upon shutdown, thus the `sys.addAddShutownHook` was added so that this example can be a little useful. Stats can be seen upon quitting this server with `control-C` or stopping it using the IDE you prefer. As an example of output, we'll observer statistics upon shutting down after a single request:

```txt
# counters
myServer/admission_control/deadline/exceeded 0
myServer/admission_control/deadline/rejected 0
myServer/closes                0
myServer/connects              1
myServer/http/status/200       1
myServer/http/status/2XX       1
myServer/nacks                 0
myServer/nonretryable_nacks    0
myServer/read_timeout          0
myServer/received_bytes        78
myServer/requests              1
myServer/sent_bytes            67
myServer/socket_unwritable_ms  0
myServer/socket_writable_ms    0
myServer/success               1
myServer/thread_usage/requests/per_thread/finagle/netty4-1 1
myServer/write_timeout         0
# gauges
myServer/connections           0.0
myServer/pending               0.0
myServer/thread_usage/requests/mean 0.0
myServer/thread_usage/requests/relative_stddev 0.0
myServer/thread_usage/requests/stddev 0.0
myServer/tls/connections       0.0
# stats
myServer/connection_duration   n=1 min=223.0 med=223.0 p90=223.0 p95=223.0 p99=223.0 p999=223.0 p9999=223.0 max=223.0
myServer/connection_received_bytes n=1 min=78.0 med=78.0 p90=78.0 p95=78.0 p99=78.0 p999=78.0 p9999=78.0 max=78.0
myServer/connection_requests   n=1 min=1.0 med=1.0 p90=1.0 p95=1.0 p99=1.0 p999=1.0 p9999=1.0 max=1.0
myServer/connection_sent_bytes n=1 min=67.0 med=67.0 p90=67.0 p95=67.0 p99=67.0 p999=67.0 p9999=67.0 max=67.0
myServer/handletime_us         n=1 min=7594.0 med=7594.0 p90=7594.0 p95=7594.0 p99=7594.0 p999=7594.0 p9999=7594.0 max=7594.0
myServer/http/response_size    n=1 min=28.0 med=28.0 p90=28.0 p95=28.0 p99=28.0 p999=28.0 p9999=28.0 max=28.0
myServer/http/time/200         n=1 min=82.0 med=82.0 p90=82.0 p95=82.0 p99=82.0 p999=82.0 p9999=82.0 max=82.0
myServer/http/time/2XX         n=1 min=82.0 med=82.0 p90=82.0 p95=82.0 p99=82.0 p999=82.0 p9999=82.0 max=82.0
myServer/request_latency_ms    n=1 min=29.0 med=29.0 p90=29.0 p95=29.0 p99=29.0 p999=29.0 p9999=29.0 max=29.0
myServer/request_payload_bytes n=1 min=0.0 med=0.0 p90=0.0 p95=0.0 p99=0.0 p999=0.0 p9999=0.0 max=0.0
myServer/response_payload_bytes n=1 min=28.0 med=28.0 p90=28.0 p95=28.0 p99=28.0 p999=28.0 p9999=28.0 max=28.0
```

Console output of stats are not as useful as sending them to a real metrics server ( promethius, APM etc ) but this example shows just the complete and unfettered statistics details.

### Service binding and program execution

Calling one of the overloaded `serve()` methods on [Http.server](http://http-finagle-server) starts the server as configured. In this case, we simply provide a [SocketAddress](http://socket-address) or a contravariant that can produce this type of object.
The final arugment to this method gives our service implementation which will handle any request seen by this server.

## Conclusion & Links

This was just a very brief overview of a powerful API. Our next mission is to provide a deeper gaze at the tooling around Finagle - Finch, Finatra, TwitterServer.

* https://blog.twitter.com/engineering/en_us/a/2011/finagle-a-protocol-agnostic-rpc-system.html

### Backpressure (Warning: MATH, LOTS OF MATH)

* http://anrg.usc.edu/www/papers/IPSN10_Moeller_Sridharan_Krishnamachari_Gnawali.pdf

### Gutfrage's Finagle Docs

* https://gutefrage.github.io/the-finagle-docs/

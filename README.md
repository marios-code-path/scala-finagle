+++
date = 2018-07-30
publishDate = 2018-08-04
title = "Intro to Finagle services with Scala and Gradle."
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

We will highlight two important building blocks for our Services to use: [Flags](https://twitter.github.io/finatra/user-guide/getting-started/flags.html), and [Modules](https://twitter.github.io/finatra/user-guide/getting-started/modules.html). To enable Modules, include `inject-server` as a dependency in your build.

```c
name := "example-service"
version := "1.0"
libraryDependencies += "com.twitter" %% "finagle-http" % "18.8.0"
libraryDependencies += "com.twitter" %% "inject-server" % "18.8.0"
```

## Quick Finagle HTTP Service

Finagle lets you develop services with configuration in mind. You may stand up a single HTTP server by extending the base trait [Service](https://twitter.github.io/finagle/guide/ServicesAndFilters.html) and specifying your `Req` and `Res` types in generic form. To see this in action, we will demonstrate HTTP service by constructing a simple `Service[http.Request, http.Response]`.

A generic Finagle service uses the [Service](https://twitter.github.io/finagle/guide/ServicesAndFilters.html) trait to expose service functionality such that any Service[Req, Res] `A` may consume `Req`, and respond with `Res`. This can be seen in the sample code below, where we use [http.Request](http://www.github.com/) and `http.Response` as the input/output types. Because Finagle is an [RPC](http://link-to-some-rpc-doc) system, we must implement the `apply(Req): Future[Res]` methods where the response is a [Future](http://Future) of the returned `http.Response`.

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

### Wrapping Services with Filters

Filters in Finagle allow us to transform a service's output. For example through the Twitter docs, a brief defintion Filter traits should suffice in this example.

```java
 * A [[Filter]] acts as a decorator/transformer of a [[Service service]].
 * It may apply transformations to the input and output of that service:
 * {{{
 *           (*  MyService  *)
 * [ReqIn -> (ReqOut -> RepIn) -> RepOut]
 * }}}
```

Given a Service provides the translation between two types `ReqOut` and `ReqIn`, our Filter allows us to `decorate` this service with additional input/output types, thus `ReqIn` and `RepOut`. Filters extend the [Service]() trait and include additional compositional method `andThen` (we will see later) that lets us stack filters and services.

For now, we can define our example filter to execute an anonymous lambda provided by in the constructor, then have it proceed with servicing the request.

```scala
package example

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future

class ExampleFilter(myFn: Unit => Unit) extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    myFn()
    service(request)
  }
}
```

## Finagle HTTP Server

Twitter/Finagle allows us to define our [Server](http://twitter-finagle-server) through the same `Req` / `Res` input/output types we defined in our Service./
Finagle comes pre-packaged with a couple protocol-specific servers that we could use to harness our Service's functionality. We will observe Finagle [Http](http://twitter-finagle-http) server capabilities, and how to configure and start the server.

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
In this (basic) example, our service responds to all HTTP URI's on our localhost Server. The configuration of the S


### Filters and Services

Composing a filtered HTTP service with Finagle is actually quite simple.  

## Harnessing TwitterServer

Lets review some basics.  First, there is [TwitterServer]() which enables us to implement fully functionling Services complete with configuration, dependency injection, tracing, logging and more.. TwitterServer does much of the work to intercept the lifecycle of your objects, and exposes ways to get at them withthem with a simple convententional API.

Our class creates a `modules` override member that we use to place a [Module]() in order to receive it's injected componenets ( like @Bean's in Spring ). We review `Modules` in depth later.  For now, we will accept that our module gives us this instance of a MyService [Service]() implementation. Because we're using a TwtiterServer class, we can access it's field members such as `injector` which I use to provide the configured `MyService` class.

```scala
class SimpleApp extends App {
  val service = new MyService(true)
  val server = Http.serve(":8080", service)

  Await.ready(server)
}
```
# unit test



### Test with Client

## Conclusion & Links

### Finagle Introduction

* https://blog.twitter.com/engineering/en_us/a/2011/finagle-a-protocol-agnostic-rpc-system.html

### Backpressure (Warning: MATH, LOTS OF MATH)

* http://anrg.usc.edu/www/papers/IPSN10_Moeller_Sridharan_Krishnamachari_Gnawali.pdf

### Gutfrage's Finagle Docs

* https://gutefrage.github.io/the-finagle-docs/

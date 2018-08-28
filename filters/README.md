+++
date = 2018-08-25
publishDate = 2018-08-27
title = "Getting started with Filters in Finagle"
description = "Finagle lets you develop and deploy services easily."
toc = true
categories = ["scala","twitter","finagle"]
tags = ["functional","scala","services","filter","demo"]
+++

# What is Finagle

In this example, we will go through the steps to deploy a Filtered HTTP Service with [Scala](https://www.scala-lang.org/) and [SBT](https://www.scala-sbt.org/).

[Finagle](https://twitter.github.io/finagle/) is an extensible [RPC](https://en.wikipedia.org/wiki/Remote_procedure_call) system for the JVM, that lets developers write non-blocking, high-concurrency services with ease. Much of what Finagle will let us do is summarized in the [doc](https://twitter.github.io/finagle/).

> Finagle implements uniform client and server APIs for several protocols, and is designed for high performance and concurrency. Most of Finagle’s code is protocol agnostic, simplifying the implementation of new protocols.

# Build with SBT

This example, and others like it will rely on a quick and simple build tool. Lets examine this [SBT](https://www.scala-sbt.org/) build for the dependencies used in this example. We only need to include "finagle-http" dependency at version 18. This is using [Finagle 6](https://twitter.github.io/finagle/guide/changelog.html).

```c
name := "sample-filters"
version := "1.0"
libraryDependencies += "com.twitter" %% "finagle-http" % "18.8.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"
```

# Services

We will define an HTTP service that returns the minimum value of a sequence of integers. The user inputs a 'next' integer, then the service emits the smallest of all values.  

```scala
package example

import com.twitter.finagle.http.{Response, Status}
import com.twitter.finagle.{Service, http}
import com.twitter.util.Future

object SampleService extends SampleService

class SampleService extends Service[http.Request, http.Response] {

  val seed = Seq(76, 69, 71, 48, 83, 42)

  def apply(req: http.Request): Future[http.Response] = {
    val response = Response(req.version, Status.Ok)
    val sample: Seq[Int] = seed ++ Seq(req.getIntParam("next",100))

    response.setContentString(s"Minimum target sample is: ${sample.min}")

    Future.value(
      response
    )
  }

}
```

# Filters

 Filters lets us change the input and output types for a given service, wrap service logic (eg with logging) and even simplify the relationship between a services configured states through composition. Lets see what Twitter docs say about Filters:

```java
 * A [[Filter]] acts as a decorator/transformer of a [[Service service]].
 * It may apply transformations to the input and output of that service:
 * {{{
 *           (*  MyService  *)
 * [ReqIn -> (ReqOut -> RepIn) -> RepOut]
 * }}}
```

Given a `Service` provides the translation between two types `ReqOut` and `ReqIn`, a `Filter` allows turn those types into new types, thus `ReqIn` and `RepOut`. Filters maintain API consistency with ordinary `Service`s through [Service](https://twitter.github.io/finagle/docs/com/twitter/finagle/Service.html) trait, with that we also get an overloaded compositional method [andThen()](https://twitter.github.io/finagle/docs/com/twitter/finagle/Service.html#close():com.twitter.util.Future[Unit]) that allows us to glue together filters and services.

To visualize what this means, lets take a look at the good docs: ![Filter Viualization](https://twitter.github.io/finagle/guide/_images/filter2.png)

# Filter Example

As a practial example, I have defined an example filter to log HTTP requests.  This is a simple re-use of the existing [LoggingFilter](https://twitter.github.io/finagle/docs/com/twitter/finagle/http/filter/LoggingFilter.html) which takes a [CommonLogFormatter](https://twitter.github.io/finagle/docs/com/twitter/finagle/http/filter/CommonLogFormatter.html) for configured log formatting.

```scala
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
    .serve(addr = ":8080", service)

  Await.ready(server)
}

```

In order to take advantage of the behaviour modificaion that this filter allows, we'll need to compose our final exposed service. We can do this with the `andThen` method used to glue `Service`s together.  The returened composition will have the same input and outputs as our original service, with the added bonus of HTTP logging.

This example uses the Http singleton object to instantiate a listening HTTP 1.1 server.
Finally, because we are running on our own synchronous main method, make sure we need to block on something for the HTTP server to have a chance to respond to requests. We can block on the HTTP Server by calling [com.twitter.util.concurrent.Await](https://twitter.github.io/util/docs/com/twitter/util/Await$.html) `.ready` method and giving our [Awaitable](https://twitter.github.io/util/docs/com/twitter/util/Awaitable.html) a chance to complete, or be interruped ( by control-c ).

We can now execute this probject using SBT.

```s
$ sbt sbt "runMain example.SampleApp"
[info] Loading settings from build.sbt ...
...
[info] Running example.SampleApp 
Aug 27, 2018 5:13:05 PM com.twitter.finagle.Init$ $anonfun$once$1
INFO: Finagle version 18.8.0 (rev=b12759650084cd4eaa890045f1f921127b368d20) built at ...
```

Console output of service show just the build time for Finagle. Because we setup a LogginFilter, we can visit the running Service and observe it's log output once we have accessed an serviceendpoint.

```s
$ curl http://localhost:8080
Minimum target sample is: 42

$ curl http://localhost:8080\?next=6
Minimum target sample is: 6
```

Whereas our logs will read with the results:

```s
...
INFO: Finagle version 18.8.0 (rev=b12759650084cd4eaa890045f1f921127b368d20) built at 20180806-152739
Aug 27, 2018 5:14:11 PM com.twitter.finagle.filter.LoggingFilter log
INFO: 0:0:0:0:0:0:0:1 - - [28/Aug/2018:00:14:11 +0000] "GET / HTTP/1.1" 200 28 13 "curl/7.54.0"
Aug 27, 2018 5:14:18 PM com.twitter.finagle.filter.LoggingFilter log
INFO: 0:0:0:0:0:0:0:1 - - [28/Aug/2018:00:14:18 +0000] "GET /?next=42 HTTP/1.1" 200 28 3 "curl/7.54.0"\
```

## Customizing your Filter

If the existing filter does not give enough infomration how Filters can be created, and we usually want some kind of custom behaviour. So lets examine what a custom filter looks like. Lets set one up by extending [SimpleFilter](https://twitter.github.io/finagle/docs/com/twitter/finagle/SimpleFilter.html) and supplying the `apply()` method. This example will simply execute an anonymous function (lambda) before completing the http request/response.

```scala
class SampleFilter(val myFn: Unit => Unit) extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    myFn()
    service(request)
  }
}
```

Now that we have our custom filter, lets compose our improved service with some logic. Back to our SampleApp, we can initialize the service with our filter and a simple and conveneint lambda.

```scala
object SampleApp extends App {
  val service: Service[Request, Response] = MyLoggingFilter
    .andThen(new SampleFilter(req => Logger().info(s"Request path: ${req.path}")))
    .andThen(SampleService)

```

Now when we access our service via `curl`, we should see output on our server's log similar to the following:

```s
Aug 27, 2018 9:03:18 PM example.SampleApp$ $anonfun$service$1
INFO: Request path: /fizzfoo
Aug 27, 2018 9:03:18 PM com.twitter.finagle.filter.LoggingFilter log
INFO: 0:0:0:0:0:0:0:1 - - [28/Aug/2018:04:03:18 +0000] "GET /fizzfoo HTTP/1.1" 200 28 19 "curl/7.54.0"

```

## Conclusion & Links

This was just a very brief overview of how Filters assist in composing our Services API. Lets just check out a few more topical bits that we might use in the future projects.

* https://blog.twitter.com/engineering/en_us/a/2011/finagle-a-protocol-agnostic-rpc-system.html

### Backpressure (Warning: MATH, LOTS OF MATH)

* http://anrg.usc.edu/www/papers/IPSN10_Moeller_Sridharan_Krishnamachari_Gnawali.pdf

### Gutfrage's Finagle Docs

* https://gutefrage.github.io/the-finagle-docs/

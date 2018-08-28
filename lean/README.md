+++
date = 2018-07-30
publishDate = 2018-08-04
title = "Intro to Finagle services with Scala and SBT."
description = "Finagle lets you develop and deploy services easily."
toc = true
categories = ["scala","twitter","finagle"]
tags = ["functional","scala","services","demo"]
+++

# What is Finagle

[Finagle](https://twitter.github.io/finagle/) is an extensible [RPC](https://en.wikipedia.org/wiki/Remote_procedure_call) system for the JVM, that lets developers write non-blocking, high-concurrency services with ease. Much of what Finagle will let us do is summarized in the [doc](https://twitter.github.io/finagle/).

> Finagle implements uniform client and server APIs for several protocols, and is designed for high performance and concurrency. Most of Finagle’s code is protocol agnostic, simplifying the implementation of new protocols.

In this example, we will go through the steps to deploy a basic Finagle HTTP Service with [Scala](https://www.scala-lang.org/) and [SBT](https://www.scala-sbt.org/).

## Build Dependencies

First, Lets examine this [SBT](https://www.scala-sbt.org/) build for the dependencies used in this example. We only need to include "finagle-http" dependency at version 18. This is using [Finagle 6](https://twitter.github.io/finagle/guide/changelog.html).

```c
name := "example-service"
version := "1.0"
libraryDependencies += "com.twitter" %% "finagle-http" % "18.8.0"
```

## Quick Finagle HTTP Service

Finagle lets you develop services in a programmatic way. You may stand up a single HTTP server by extending the base trait [Service](https://twitter.github.io/finagle/guide/ServicesAndFilters.html) and specifying your `Req` (request) and `Res` (Result) types in generic form. To see this in action, we will demonstrate HTTP service by constructing a simple `Service[http.Request, http.Response]` where request and response are HTTP I/O types.

A generic Finagle service uses the [Service](https://twitter.github.io/finagle/guide/ServicesAndFilters.html) trait to expose service functionality such that any `Service[Req, Res]` named `A` may consume its input `Req`, and respond with `Res`. This can be seen in the sample code below, where we use [http.Request](https://twitter.github.io/finagle/docs/com/twitter/finagle/http/Request.html) and [http.Response](https://twitter.github.io/finagle/docs/com/twitter/finagle/http/Response.html) as the input/output types. Because Finagle is an [RPC](https://en.wikipedia.org/wiki/Remote_procedure_call) system, we must implement the `apply(Req): Future[Res]` methods where the return type is a [com.twitter.util.Future](https://twitter.github.io/util/docs/com/twitter/util/Future.html) of the response.

```scala
package example

import com.twitter.finagle.http.{Response, Status}
import com.twitter.finagle.{Service, http}
import com.twitter.util.Future

object MyService extends MyService

class MyService extends Service[http.Request, http.Response] {

  val seed = Seq(76, 69, 71, 48, 83, 42)

  def apply(req: http.Request): Future[http.Response] = {
    val response = Response(req.version, Status.Ok)
    val sample = (seed ++
      Seq(
        req.getLongParam(name = "next", default = 100)
      )).min

    response.setContentString(s"Minimum target sample is: ${sample}")

    Future.value(
      response
    )
  }

}
```

## The Finagle HTTP Server

Finagle allows us to define our listening [Server](https://twitter.github.io/finagle/docs/com/twitter/finagle/server/index.html) with the same `Req` / `Res` types we defined in our Service.
Finagle comes pre-packaged with a couple protocol-specific servers that we could use to harness our Service's functionality. We will observe Finagle [Http](https://twitter.github.io/finagle/guide/Quickstart.html) server capabilities, and use it to expose our Service.

In this example, our Server will not not know about routing specifics. Of course this means that as configured, our server will respond to all HTTP URI's on port 8080. This may make sense in a proxy-routed service environment. Of course, this is a very lean example, and as such we'll take no corners in standing up a simple listening HTTP server.

```scala
package example

import com.twitter.finagle.{Http, Service, http}
import com.twitter.util.Await

object ServerApp extends App {

  val service: Service[http.Request, http.Response] = MinService

  val server = Http.server
    .serve(addr = ":8080", service)

  Await.ready(server)
}
```

### Service binding and program execution

Calling one of the overloaded `serve()` methods on [Http.server](http://http-finagle-server) starts the server as configured. In this case, we simply provide a [SocketAddress](http://socket-address) or a covariant that can produce this type of object.
The final arugment to this method gives our service implementation which will handle any request seen by this server.

This example uses the Http singleton object to instantiate a listening HTTP 1.1 server.
Finally, because we are running on our own synchronous main method, make sure we need to block on something for the HTTP server to have a chance to respond to requests. We can block on the HTTP Server by calling [com.twitter.util.concurrent.Await](https://twitter.github.io/util/docs/com/twitter/util/Await$.html) `.ready` method and giving our [Awaitable](https://twitter.github.io/util/docs/com/twitter/util/Awaitable.html) a chance to complete, or be interruped ( by control-c ).

We can now execute this probject using SBT.

```s
$ sbt "runMain example.ServerApp"
[info] Loading settings from build.sbt ...
...
[info] Running example.ServerApp
Aug 25, 2018 5:02:12 PM com.twitter.finagle.Init$ $anonfun$once$1
INFO: Finagle version 18.8.0 (rev=b12759650084cd4eaa890045f1f921127b368d20) built at 20180806-152739
```

Console output of service show just the build time for Finagle. This example doesnt configure logging. For that, see the [next artice](http://www.google.com/404).

To show output for our service, execute the curl command to the configured port and host. Optionally include the parameter 'next' to add some variance to it's output.

```s
$ curl http://localhost:8080
Minimum target sample is: 42

$ curl http://localhost:8080\?next=6
Minimum target sample is: 6
```

## Conclusion & Links

This was just a very brief overview of a powerful API. Our next mission is to provide a deeper gaze at the tooling around Finagle - Finch, Finatra, TwitterServer.

* https://blog.twitter.com/engineering/en_us/a/2011/finagle-a-protocol-agnostic-rpc-system.html

### Backpressure (Warning: MATH, LOTS OF MATH)

* http://anrg.usc.edu/www/papers/IPSN10_Moeller_Sridharan_Krishnamachari_Gnawali.pdf

### Gutfrage's Finagle Docs

* https://gutefrage.github.io/the-finagle-docs/
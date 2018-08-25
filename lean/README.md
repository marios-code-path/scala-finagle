ope+++
date = 2018-07-30
publishDate = 2018-08-04
title = "Intro to Finagle services with Scala and SBT."
description = "Finagle lets you develop and deploy services easily."
toc = true
categories = ["scala","twitter","finagle"]
tags = ["functional","scala","services","demo"]
+++
# Standing up HTTP/RPC services with Finagle API

## Why Finagle

[Finagle](https://twitter.github.io/finagle/) is a Service building framework that lets developers write non-blocking services with ease. In this example, we will go through the steps to deploy a basic Finagle Service that takes advantage of the Twitter services eco-system in TwitterServer, Guice and of coure Scala.

### JVM Functions in Scala

Finagle is written in Scala, and works best in applications - scala or java - that want to take the [functional services](https://monkey.org/~marius/funsrv.pdf) approach. You can prefer to choose ordinary Java or some other JVM language.

### Build Dependencies

We will highlight two important building blocks for our program. First Lets examine this  [SBT](http://sbt) build for the dependencies used in this example.

```c
name := "example-service"
version := "1.0"
libraryDependencies += "com.twitter" %% "finagle-http" % "18.8.0"
```

## Quick Finagle HTTP Service

Finagle lets you develop services in a conventional, programatic and functional way. You may stand up a single HTTP server by extending the base trait [Service](https://twitter.github.io/finagle/guide/ServicesAndFilters.html) and specifying your `Req` (request) and `Res` (Result) types in generic form. To see this in action, we will demonstrate HTTP service by constructing a simple `Service[http.Request, http.Response]`.

A generic Finagle service uses the [Service](https://twitter.github.io/finagle/guide/ServicesAndFilters.html) trait to expose service functionality such that any `Service[Req, Res]` named `A` may consume its input `Req`, and respond with `Res`. This can be seen in the sample code below, where we use [http.Request](http://www.github.com/) and `http.Response` as the input/output types. Because Finagle is an [RPC](http://link-to-some-rpc-doc) system, we must implement the `apply(Req): Future[Res]` methods where the response is a [Future](http://Future) of the returned `Res` type `http.Response`.

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

Finagle allows us to define our [Server](http://twitter-finagle-server) through the same `Req` / `Res` input/output types we defined in our Service./
Finagle comes pre-packaged with a couple protocol-specific servers that we could use to harness our Service's functionality. We will observe Finagle [Http](http://twitter-finagle-http) server capabilities, and use it to expose our Service.

In this example, our `MyService` service does not know about routing specifics. Of course this means that as configured, our server will respond to all HTTP URI's on port 8080. This may make sense in a proxy-routed service environment. Of course, this is a very lean example, and as such we'll take no turns to get a simple server working.

Finally, because we are running on our own synchronous main method, make sure we need to block on something for the HTTP server to have a chance to respond to requests. We can block on the HTTP Server by calling [com.twitter.util.concurrent.Await](http://await) `.ready` method and giving our [Awaitable](http://awaitable) a chance to complete, or be interruped ( by control-c ).

```scala
package example

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await

object FinagleApp extends App {

  val service: Service[Request, Response] = MyService

  val server = Http.server
    .serve(addr = ":8080", service)

  Await.ready(server)
}
```

Execute this probject using sbt.

```s
$ sbt "runMain example.MyServer"

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

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

## A Simple Finagle HTTP Service

Finagle lets you develop services with configuration in mind. You may stand up a single HTTP server by extending the base trait [Service](https://twitter.github.io/finagle/guide/ServicesAndFilters.html) and specifying your Input and Output types in generic form. To see this in action, we will demonstrate HTTP service by constructing a simple `Service[http.Request, http.Response]`.

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

## Finagle HTTP Server

At it's most basic and simplest form, Twitter/Finagle allows us to define our [Server](http://twitter-finagle-server) through the same `Req` / `Res` input/output types we defined in our Service./
Finagle comes pre-packaged with a couple protocol-specific servers that we could use to harness our Service's functionality. We will observe Finagle [Http](http://twitter-finagle-http) server capabilities, and how to configure and start the server.

```scala
import com.twitter.finagle.Http
import com.twitter.finagle.stats.NullStatsReceiver
import com.twitter.finagle.tracing.NullTracer
import com.twitter.util.Await

object FinagleApp extends App {
  
  val server = Http.server
    .withStatsReceiver(NullStatsReceiver)
    .withLabel("example")
    .serve(addr = ":8080", service = new MyService(true))

  Await.ready(server)
}
```

### Stacks

Finagle uses a pattern called `stacking`.  We configure with `Http.server` followed by configuration methods. In this example, we re-use safe defaults for the Http Implementation - namely Netty 4, s


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


### A Simple HTTP Client

## Conclusion & Links

### Finagle Introduction

* https://blog.twitter.com/engineering/en_us/a/2011/finagle-a-protocol-agnostic-rpc-system.html

### Backpressure (Warning: MATH, LOTS OF MATH)

* http://anrg.usc.edu/www/papers/IPSN10_Moeller_Sridharan_Krishnamachari_Gnawali.pdf

### Gutfrage's Finagle Docs

* https://gutefrage.github.io/the-finagle-docs/

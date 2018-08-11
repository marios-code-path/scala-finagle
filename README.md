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

### SBT

We will highlight two important building blocks for our Services to use: [Flags](https://twitter.github.io/finatra/user-guide/getting-started/flags.html), [Modules](https://twitter.github.io/finatra/user-guide/getting-started/modules.html)

```c
name := "quickstart"
version := "1.0"
libraryDependencies += "com.twitter" %% "finagle-http" % "18.8.0"

```

### Maybe Gradle

## Code

### A Simple HTTP Service

```java
class MyService(showMinimum: Boolean) extends Service[http.Request, http.Response] {

  val seed = Seq(42, 75, 29, 64, 88)

  def apply(req: http.Request): Future[http.Response] = {
    val response = Response(req.version, Status.Ok)
    val sample = if (showMinimum) seed.min else seed.max
    val string = if (showMinimum) "Minimum" else "Maximum"

    response.setContentString(s"${string} sample is: ${sample}")

    Future.value(
      response
    )
  }

}
```

### A Simple HTTP Client

## Conclusion & Links

### Finagle Introduction

* https://blog.twitter.com/engineering/en_us/a/2011/finagle-a-protocol-agnostic-rpc-system.html

### Backpressure (Warning: MATH, LOTS OF MATH)

* http://anrg.usc.edu/www/papers/IPSN10_Moeller_Sridharan_Krishnamachari_Gnawali.pdf

### Another False Math Proof

* https://www.math.hmc.edu/funfacts/ffiles/10001.1-8.shtml

### Services for scala.

*

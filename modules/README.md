+++
date = 2018-08-25
publishDate = 2018-08-27
title = "Configuration through Commandline Flags with Twitter Server"
description = "Configure App behaviour with Flags"
toc = true
categories = ["scala","twitter","finagle", "flags"]
tags = ["functional","scala","services","filter", "flags", "commandline" , "cli", "demo"]
+++

# What is Finagle

In this example, we will go through the steps to deploy a HTTP Service with [Scala](https://www.scala-lang.org/) and [SBT](https://www.scala-sbt.org/). In addition to the basics, we will add service behaviour through the use of the [Flag](https://twitter.github.io/finatra/user-guide/getting-started/flags.html) API.

If this is your first time seeing Finagle, then I would suggest you take a look at the Finagle [intro document](https://github.com/marios-code-path/scala-finagle/tree/master/lean) which describes how to code and run a simple HTTP service.

# Build with SBT

This example, and others like it will rely on a quick and simple build tool. Lets examine this [SBT](https://www.scala-sbt.org/) build for the dependencies used in this example. We only need to include "finagle-http" dependency at version 18. This is using [Finagle 6](https://twitter.github.io/finagle/guide/changelog.html).

```c
name := "sample-filters"
version := "1.0"
libraryDependencies += "com.twitter" %% "finagle-http" % "18.8.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"
libraryDependencies += "com.twitter" %% "inject-server" % "18.8.0"
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

# Flags

# Flag Example

## Conclusion & Links

This example is just scratching the surface of the potential capabilities with developing your services in Finagle.

* https://blog.twitter.com/engineering/en_us/a/2011/finagle-a-protocol-agnostic-rpc-system.html

### Backpressure (Warning: MATH, LOTS OF MATH)

* http://anrg.usc.edu/www/papers/IPSN10_Moeller_Sridharan_Krishnamachari_Gnawali.pdf

### Gutfrage's Finagle Docs

* https://gutefrage.github.io/the-finagle-docs/

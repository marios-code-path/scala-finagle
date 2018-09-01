+++
date = 2018-08-27
publishDate = 2018-08-29
title = "Server Flags with Twitter Finagle"
description = "Program Switches (Flags) in Finagle Applications with Scala"
toc = true
categories = ["scala","twitter","finagle", "flags"]
tags = ["functional","scala","services","filter", "flags", "commandline" , "cli", "demo"]
+++

# This Example

In this example, we setup and run an HTTP Service with [Scala](https://www.scala-lang.org/) and [SBT](https://www.scala-sbt.org/). In addition to the basics, we'll dictate service behaviour through the use of [Flags](https://twitter.github.io/finatra/user-guide/getting-started/flags.html) API.

If this is your first time seeing Finagle, then I would suggest you take a look at the Finagle [intro document](https://github.com/marios-code-path/scala-finagle/tree/master/lean) which describes how to code and run a simple HTTP service.

# Build with SBT

This example, and others like it will rely on a quick and simple build tool. Lets examine this [SBT](https://www.scala-sbt.org/) build for the dependencies used in this example. We only need to include "finagle-http" dependency at version 18. This is using [Finagle 6](https://twitter.github.io/finagle/guide/changelog.html).

```c
name := "app-flags"
version := "1.0"
libraryDependencies += "com.twitter" %% "finagle-http" % "18.8.0"
libraryDependencies += "com.twitter" %% "inject-server" % "18.8.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"
```

# Flags

- What are flags
  command line driven program values
- How to use Flags
  - app main
  - modules
- When to use Flags

Consider the following flag definition, that appears in our `SampleApp`. A flag may be defined with it's switch label, default value, and it's help dialogue. Flags are simple and support converting composite types that are String convertible into Scala values. This type safe feature lets us focus on simplicity and safety.

App.scala:

```scala
  object MyApp extends com.twitter.app.App {
    override def failfastOnFlagsNotParsed: Boolean = true
    val servicePort = flag("port", new InetSocketAddress(8080), "Specify TCP port to listen on")
    // ... later ...
    val server = Http.serve(servicePort(), service)
  }
```

According to Finagle Docs, Flags should only be constructed in the constructor, and should only be read in the `premain` or later, after they have been parsed. We start using flags with apps that extend [com.twitter.app.App](), including it's popular descendents: [TwitterServer]() and Finatra's [HttpServer]().

We may want our program to exit if something tries to parse a flag before it's ready. With this in mind. we should turn on the [failfastOnFlagsNotParsed]() so that our app will throw `IllegalStateException` upon unsuccessful flag value parsing.

Flags are command-line driven operator switches. Much of the functionality placed on flags should change high-level systemic behaviour, rather than features.  As an alternative to fine-grained feature modification,consider using Toggles [ToggleMap](http://) or even [Tunables](http://).

SampleService.scala:

```scala
package example

import com.twitter.finagle.http.{Response, Status}
import com.twitter.finagle.{Service, http}
import com.twitter.util.Future

class SampleService(val reverse: Boolean) extends Service[http.Request, http.Response] {

  def apply(req: http.Request): Future[http.Response] = {
    val response = Response(req.version, Status.Ok)
    val sample = req.getParam("name").toUpperCase()

    response.setContentString(if (!reverse) sample else sample.reverse)

    Future.value(
      response
    )
  }
}
```

The behaviour of our service is modified by the flag in the SampleModule module. This flag simply switches on the services reversing capabilities.. The service requires an argument at construction time in order to switch behaviours.

# Modules

A Module is a dependency injection unit. it lets us compose the state of our objects with type-level dependency specification. For example, in our SampleModule, we have a Service that is provided using @Singleton and @Provides annotations to indicate we want a single instance of an dependency managed object.  Injectors will ensure any arguments get fed to our Service.

SampleModule.scala:

```scala
package example

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.TwitterModule

object SampleModule extends TwitterModule {
  val flagReverse = flag("reverse", false, "Reverses the string.")

  @Singleton
  @Provides
  def providesSampleService: SampleService = new SampleService(flagReverse.apply())

}
```

## Obtaining Instance from Guice

## Module lifecycles

## Caveats with Multi-Module

## Conclusion & Links

* https://blog.twitter.com/engineering/en_us/a/2011/finagle-a-protocol-agnostic-rpc-system.html

### Backpressure (Warning: MATH, LOTS OF MATH)

* http://anrg.usc.edu/www/papers/IPSN10_Moeller_Sridharan_Krishnamachari_Gnawali.pdf

### Gutfrage's Finagle Docs

* https://gutefrage.github.io/the-finagle-docs/

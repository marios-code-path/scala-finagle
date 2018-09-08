+++
date = 2018-08-27
publishDate = 2018-09-02
title = "Server Flags with Twitter Finagle"
description = "Program Switches (Flags) in Finagle Applications with Scala"
toc = true
categories = ["scala","twitter","finagle", "flags"]
tags = ["functional","scala","services","filter", "flags", "commandline" , "cli", "demo"]
+++

# This Example

In this example, we setup and run an HTTP Service with [Scala](https://www.scala-lang.org/) and [SBT](https://www.scala-sbt.org/). We'll cover the configuration of our services with the Finagle[Flags](https://twitter.github.io/finatra/user-guide/getting-started/flags.html) API.

If this is your first time seeing Finagle, then I would suggest you take a look at the Finagle [intro document](https://github.com/marios-code-path/scala-finagle/tree/master/lean) which describes how to code and run a simple HTTP service.

# Build with SBT

This example, and others like it will rely on a quick and simple build tool. Lets examine this [SBT](https://www.scala-sbt.org/) build for the dependencies used in this example. We only need to include "finagle-http" dependency at version 18. This is using [Finagle 6](https://twitter.github.io/finagle/guide/changelog.html).

```c
name := "app-flags"
version := "1.0"
libraryDependencies += "com.twitter" %% "finagle-http" % "18.8.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"
```

# What are Flags

Consider the following flag definition, that appears in our `SampleApp`. A flag may be defined by it's switch label, default value and it's help dialogue. Flags are simple and support converting composite types that are [String](http://rick-roll-dot-com) convertible into Scala values. This type safe feature lets us focus on simplicity and safety.

App.scala:

```scala
object SampleApp extends com.twitter.app.App {
  val servicePort = flag("port", new InetSocketAddress(8080), "Specify TCP port to listen on")
  val flagReverse = flag("reverse", false, "Reverses the string.")

  override val failfastOnFlagsNotParsed = true

   premain {
      val service = new SampleService(flagReverse())
      val server = Http.serve(servicePort(), service)

      Await.ready(server)
  }
}
```

## How and when to use

According to Finagle Docs, Flags should only be constructed in the constructor, and should only be read in the `premain` or later, after they have been parsed. We start using flags with apps that extend [com.twitter.app.App](https://twitter.github.io/util/docs/com/twitter/app/App.html), including it's popular descendents: [TwitterServer](https://github.com/twitter/twitter-server) and Finatra's [HttpServer](https://twitter.github.io/finatra/user-guide/http/server.html).

We may want our program to exit if something tries to parse a flag before it's ready. With this in mind. we should turn on the (boolean) `failfastOnFlagsNotParsed` property so that our app will throw `IllegalStateException` upon unsuccessful flag access.

## When to use

Flags are command-line driven operator switches. Much of the functionality placed on flags should change high-level systemic behaviour, rather than features.  As an alternative to fine-grained feature modification,consider using Toggles [ToggleMap](https://twitter.github.io/finagle/docs/com/twitter/finagle/toggle/ToggleMap.html) or even [Tunables](https://twitter.github.io/finagle/docs/com/twitter/finagle/tunable/index.html). The later will be demonstrated in forthcoming examples.

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

This flag simply switches on the services reversing capabilities. The service requires an argument at construction time in order to switch behaviours. We also supply a TCP port number override to listen on. This is useful when you want to spin up services on separate ports.

# Execute

To run our demonstration simply use the command line to execute sbt.

```sh
~ ❯❯❯ sbt "runMain example.SampleApp -port 8081 -reverse"
[info] Running example.SampleApp -port=:8081 -reverse=true
MONTH DAY, YEAR HH:MM:SS AMPM com.twitter.finagle.Init$ $anonfun$once$1
INFO: Finagle version 18.8.0 (rev=b12759650084cd4eaa890045f1f921127b368d20) built at 20180806-152739
```

Access the newly defined service using [httpie](https://httpie.org/) a useful and nifty tool for accessing HTTP services.

```sh
~ ❯❯❯ http :8081\?name=example
HTTP/1.1 200 OK
Content-Length: 7

ELPMAXE
```

Thats it. Running the same service without `-reverse` yelds a slightly different output when accessed in the same mannor.


```sh
~ ❯❯❯ http :8081\?name=example
HTTP/1.1 200 OK
Content-Length: 7

EXAMPLE
```

Thats all for now. Flags are easy and powerful. Actually using them in your Day to Day services makes sense becsuse many times you'll have to configure MYSQL, MONGO, Filesystem, etc... paths. This conventional and extendable API will be revisited later on do demonstrate expanded usage such as modules, and composability scenarios. We'll leave for now with links to gain greater awareness of the Finagle platform.

Till next time!

## Conclusion & Links

* https://blog.twitter.com/engineering/en_us/a/2011/finagle-a-protocol-agnostic-rpc-system.html

### Backpressure (Warning: MATH, LOTS OF MATH)

* http://anrg.usc.edu/www/papers/IPSN10_Moeller_Sridharan_Krishnamachari_Gnawali.pdf

### Gutfrage's Finagle Docs

* https://gutefrage.github.io/the-finagle-docs/

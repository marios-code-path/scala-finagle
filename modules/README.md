+++
date = 2018-09-07
publishDate = 2018-09-07
title = "Finagle Modules"
description = "Modules help define service behaviour"
toc = true
categories = ["scala","twitter","finagle", "modules"]
tags = ["functional", "scala", "services", "filter", "modules", "guice", "dependency-injection", "di"]
+++

# Motivation

Building applications by glueing resources together the manual way is daunting. Not impossible, but daunting. Lazy resource loading aka Dependency Injection (DI) can make your application more resilient to changes, and bring additional configurability to your project.

In this example, we will tackle managing server functionality using a few common twitter-module support guidelines.

# The Service

Lets define the meat of our sample which is a service that will answer our requests. This service simply returns a random string prefixed by the letter `T` , or a reversed random string with that prefix.  The constructor arguments are what we will use to highlight configuration with modules.

```scala
package example

import com.google.inject.Provider
import com.twitter.finagle.http.{Response, Status}
import com.twitter.finagle.{Service, http}
import com.twitter.util.Future

// just a string provider with upper-casing and reversing
class SampleService(val reverse: Boolean, val shortStrings: Provider[String]) extends Service[http.Request, http.Response] {

  def apply(req: http.Request): Future[http.Response] = {
    val response = Response(req.version, Status.Ok)
    val sample = req.getParam("name", shortStrings.get).toUpperCase()

    response.setContentString(if (!reverse) sample else sample.reverse)

    Future.value(
      response
    )
  }
}
```

Thus, we can simply bake in an instance of this service by declaring it with `new` keyword.

```scala
val service = new SampleService(false, "T" + RandomStringUtils.randomAlphabetic(8))
```

But this will make testing harder, and locks our service into a specific behaviour.  Lets take a look at customizing the service instantiation with modules.

# Declaring a Module

Modules can use [JSR-330 annotations](https://github.com/google/guice/wiki/JSR330) as a way of declaring dependency metadata. We simply provide the implementation and instance scope we will need for our specific services. Additionally, this module bundles it's own flag. Its a scoping mechanism we can use to isolate specific feature flags per module.

In order to take full advantage of scala/guice, you must extend the base class [com.twitter.inject.TwitterModule](https://github.com/twitter/finatra/blob/develop/inject/inject-core/src/main/scala/com/twitter/inject/TwitterModule.scala). This enables us to compose additioanl modules into our custom module. The canonical way to install modules with Finagle apps is to use the `override modules: Seq[Module]` construct to tell Guice which modules we will expect to have loaded. We will use this convention when we get to our app.  

SampleModule.scala:

```scala
package example

import com.google.inject.{Provider, Provides, Singleton}
import com.twitter.inject.TwitterModule
import org.apache.commons.lang.RandomStringUtils

object SampleModule extends TwitterModule {
  val flagReverse = flag("reverse", false, "Reverses the string.")

  @Provides
  def providesRandomString: String = "T" + RandomStringUtils.randomAlphabetic(8)

  @Singleton
  @Provides
  def providesSampleService(prov: Provider[String]): SampleService = new SampleService(flagReverse.apply(), prov)
}
```

This module simply exports a command-line flag, a random string generator, and finally exposes our Service as singleton dependencies for our app.

## Configuring the App with modules

We can start to consume our module with our server by extending the [com.twitter.inject.app.App](https://twitter.github.io/finatra/scaladocs/com/twitter/inject/app/App.html) as our App object. This server implementation gives us a much broader way to define our service and it's dependnecies than the regular `scala.app` or `com.twitter.app.App`. It provides [Guice](https://github.com/google/guice) injectors, flag configuration and service lifecycle hooks among others.  We will only discuss the module portion in this example.

Similar to standard Module definitions, we can also use the `override modules` sequence to install any modules we may expect during runtime.

```scala
package example

import java.net.InetSocketAddress

import com.twitter.finagle.Http
import com.twitter.util.Await

object SampleApp
  extends com.twitter.inject.app.App {
  override val modules = Seq(SampleModule)

  val flagServicePort = flag("port", new InetSocketAddress(8080), "Specify TCP port to listen on")

  postmain {
    val server = Http.serve(flagServicePort(), injector.instance[SampleService])

    onExit {
      server.close()
    }

    Await.ready(server)
  }
}
```

This app simply declares our single `SampleModule` dependency, which will provide all of the necessary injectable components needed to furnish our sample service. Because flags must be created in the constructor, and read after `main()`, we use the `postmain` lifecycle hook to do all our heavy service lifting.

# Build and Execute with SBT

This example, and others like it relies on a quick and simple build tool. Lets examine this [SBT](https://www.scala-sbt.org/) build for the dependencies used in this example. We only need to include "finagle-http" dependency at version 18. This is using [Finagle 6](https://twitter.github.io/finagle/guide/changelog.html).

```c
name := "sample-modujle"
version := "1.0"
libraryDependencies += "com.twitter" %% "finagle-http" % "18.8.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"
libraryDependencies += "com.twitter" %% "inject-server" % "18.8.0"
```

## Execute

Start the server by calling:

```sh
~ ❯❯❯ sbt "runMain example.SampleApp -reverse"
...
INFO: Finagle version 18.8.0 (rev=b12759650084cd4eaa890045f1f921127b368d20) built at 20180806-152739
```

We can interact with it using the [httpie](https://httpie.org/) commandline tool.

```sh
~ ❯❯❯ http localhost:8080
HTTP/1.1 200 OK
Content-Length: 9

TOFNDSRTE
~ ❯❯❯
```

# Conclusion & Links

This example is just scratching the surface of the potential capabilities with developing your services in Finagle.

* https://blog.twitter.com/engineering/en_us/a/2011/finagle-a-protocol-agnostic-rpc-system.html

### Backpressure (Warning: MATH, LOTS OF MATH)

* http://anrg.usc.edu/www/papers/IPSN10_Moeller_Sridharan_Krishnamachari_Gnawali.pdf

### Gutfrage's Finagle Docs

* https://gutefrage.github.io/the-finagle-docs/

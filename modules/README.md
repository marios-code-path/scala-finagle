+++
date = 2018-09-07
publishDate = 2018-09-07
title = "Service Modularity in the Finagle Framework"
description = "Using Modules to compose services."
toc = true
categories = ["scala","twitter","finagle", "modules"]
tags = ["functional", "scala", "services", "filter", "modules", "guice", "dependency-injection", "di"]
+++

# Motivation

Typical examples of dependency Injection [DI](https://stackoverflow.com/questions/130794/what-is-dependency-injection) show that the decoupling gained from removing glue code provides better testability, separation of concerns, and readability. Building applications by glueing resources together the manual way is OK, but not preferred when services need to be adapted in many ways. Dependency Injection (DI) can make your application more resilient to changes, and bring additional configurability to your project.

Finagle, uses the Guice library as it's foundation for building modules. In this example, we will tackle managing server functionality using a few key module-writing guidelines. We will tackle:

* Service composition
* Module composition
* Command-line flags composition 

# The Service

Lets define the meat of our sample which is a service that will answer our requests. This service will repeat or reverse values sent by the HTTP param `"name"`. If no parameter is there, then a random string will be generated and the ensuing effects will remain the same. Thus, our service can be summarized by the function `f(string) = (string || "T"+random).upper.reverse`. In this example, we'll use the service's dependecy on toggling reversing capability, and string generation to highlight modules/component cohesion.

```scala
package example

import com.twitter.finagle.http.{Response, Status}
import com.twitter.finagle.{Service, http}
import com.twitter.util.Future

class SampleService(isReversing: Boolean,
                    stringMaker: StringMaker) extends Service[http.Request, http.Response] {
  def apply(req: http.Request): Future[http.Response] = {
    val response = Response(req.version, Status.Ok)
    val sample = req.getParam("name", stringMaker()).toUpperCase()

    response.setContentString(if (!isReversing) sample else sample.reverse)

    Future.value(
      response
    )
  }
}
```

This HTTP service needs to know if it will reverse string, and how to make new strings (when neecessary).
Actually, a common Dependency Injection contrast scenario shows how we can simply bake in an instance of this service by declaring it with `new` keyword.

```scala
val service = new SampleService(false, _ => "T" + RandomStringUtils.randomAlphabetic(8))
```

But this will make testing harder, and locks our service into a specific behaviour among other side-effects.  Lets take a look at building the service instance with our own module instaed.

# Declaring a Module

Similar to popular DI frameworks like Spring and Dagger, Guice resources may be declared with [JSR-330 annotations](https://github.com/google/guice/wiki/JSR330) annotation metadata. Guice modules go a step further by packaging specific resources together. This way, we can create the right resources when we need them. For example, a Production-module vs Stage-module.

In order to take full advantage of Guice, you must extend the base class [com.twitter.inject.TwitterModule](https://github.com/twitter/finatra/blob/develop/inject/inject-core/src/main/scala/com/twitter/inject/TwitterModule.scala). This enables us to compose additional modules into our custom module. The canonical way to install modules with Finagle apps (and other modules) is to use the `override modules: Seq[Module]` construct to tell Guice which modules we will expect to have loaded. We will use this convention when we get to our app.

SampleModule.scala:

```scala
package example

import com.google.inject.{Provider, Provides, Singleton}
import com.twitter.inject.{Logging, TwitterModule}
import org.apache.commons.lang.RandomStringUtils

trait StringMaker extends (Unit => String)

object SampleModule extends TwitterModule with Logging {
  val flagReverse = flag("reverse", false, "Reverses the string.")

  @Provides
  def providesRandomString: StringMaker = _ => "T" + RandomStringUtils.randomAlphabetic(8)

  @Singleton
  @Provides
  def providesSampleService(prov: Provider[StringMaker]): SampleService = new SampleService(flagReverse.apply(), prov)
}
```

In this example, note that that we used the `Provider` interface for accessing our random string maker. Guices use the [Provider](https://github.com/google/guice/wiki/InjectingProviders) mechanism to instantiate and send resource instances. It also is what backs our `@Provider` annotated methods. This allows the developer to specify some additional creation logic that would ordinarily become a business detail. Alternatively, we could issue the direct type expected at constuction time, however that would have the effect of eliminating said lifecycle variability. 

Whats more, is the use of `@Singleton` scope. Basically, like other DI frameworks, this makes a single instance of our resource.

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

Obtaining an injected instance of any bean from our App main is simple using the provided [injector]() instance given in `com.twitter.inject.app.App`. Our sample makes use of it to fetch the Service resource that we bundled in the `SampleModule`.

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

# Filters and stuff

yeah lets do this.

## Extending Service Features with Filters

 Filters lets us change the input and output types for a given service, wrap service logic (eg with logging) and even simplify the relationship between a services configured states through composition. Lets see what Twitter docs say about Filters:

```java
 * A [[Filter]] acts as a decorator/transformer of a [[Service service]].
 * It may apply transformations to the input and output of that service:
 * {{{
 *           (*  MyService  *)
 * [ReqIn -> (ReqOut -> RepIn) -> RepOut]
 * }}}
```

Given a `Service` provides the translation between two types `ReqOut` and `ReqIn`, a `Filter` allows turn those types into new invariant types, thus `ReqIn` and `RepOut`. Filters maintain API consistency with ordinary `Service`s through [Service]() trait, with that we also get an overloaded compositional method `andThen()` that allows us to glue together filters and services.

For now, we can define our example filter to execute an anonymous lambda provided by in the constructor, then proceed with servicing the request.

```scala
package example

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future

class ExampleFilter(myFn: Unit => Unit) extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    myFn()
    service(request)
  }
}
```

## Next Article: Harnessing TwitterServer

Lets review some basics.  First, there is [TwitterServer]() which enables us to implement fully functionling Services complete with configuration, dependency injection, tracing, logging and more.. TwitterServer does much of the work to intercept the lifecycle of your objects, and exposes ways to get at them withthem with a simple convententional API.

Our class creates a `modules` override member that we use to place a [Module]() in order to receive it's injected componenets ( like @Bean's in Spring ). We review `Modules` in depth later.  For now, we will accept that our module gives us this instance of a MyService [Service]() implementation. Because we're using a TwtiterServer class, we can access it's field members such as `injector` which I use to provide the configured `MyService` class.

```scala
class SimpleApp extends App {
  val service = new MyService(true)
  val server = Http.serve(":8080", service)

  Await.ready(server)
}
```

### Build Dependencies

We will highlight two important building blocks for our Services to use: [Flags](https://twitter.github.io/finatra/user-guide/getting-started/flags.html), and [Modules](https://twitter.github.io/finatra/user-guide/getting-started/modules.html). To enable Modules, include `inject-server` as a dependency in your build.

```c
name := "example-service"
version := "1.0"
libraryDependencies += "com.twitter" %% "finagle-http" % "18.8.0"
libraryDependencies += "com.twitter" %% "inject-server" % "18.8.0"
```

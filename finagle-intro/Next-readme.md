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

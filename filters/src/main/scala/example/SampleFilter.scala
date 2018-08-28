package example

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future

// Example Filter that takes a function from the programmer
// Will evaluate to Request => Future[Unit]
class SampleFilter(val myFn: Request => Unit) extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    myFn(request)
    service(request)
  }
}


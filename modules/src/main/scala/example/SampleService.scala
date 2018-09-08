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
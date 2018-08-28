package example

import com.twitter.finagle.http.{Response, Status}
import com.twitter.finagle.{Service, http}
import com.twitter.util.Future

// its an uppercaser!
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
package example

import com.google.inject.{Inject, Provider}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.finagle.{Service, http}
import com.twitter.util.Future

class SampleService(isReversing: Boolean,
                    stringMaker: Provider[StringMaker]) extends Service[http.Request, http.Response] {

  def apply(req: http.Request): Future[http.Response] = {
    val response = Response(req.version, Status.Ok)
    val sample = req.getParam("name", stringMaker.get().apply()).toUpperCase()

    response.setContentString(if (!isReversing) sample else sample.reverse)

    Future.value(
      response
    )
  }
}
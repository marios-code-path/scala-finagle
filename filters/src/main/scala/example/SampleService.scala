package example

import com.twitter.finagle.http.{Response, Status}
import com.twitter.finagle.{Service, http}
import com.twitter.util.Future

object SampleService extends SampleService

class SampleService extends Service[http.Request, http.Response] {

  val seed = Seq(76, 69, 71, 48, 83, 42)

  def apply(req: http.Request): Future[http.Response] = {
    val response = Response(req.version, Status.Ok)
    val sample: Seq[Int] = seed ++ Seq(req.getIntParam("next",100))

    response.setContentString(s"Minimum target sample is: ${sample.min}")

    Future.value(
      response
    )
  }

}

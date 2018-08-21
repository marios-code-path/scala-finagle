package example

import com.twitter.finagle.http.{Response, Status}
import com.twitter.finagle.{Service, http}
import com.twitter.util.Future

class MyService(showMinimum: Boolean) extends Service[http.Request, http.Response] {

  val seed = Seq(76, 69, 71, 48, 83, 42)

  def apply(req: http.Request): Future[http.Response] = {
    val response = Response(req.version, Status.Ok)
    val sample = if (showMinimum) seed.min else seed.max
    val string = if (showMinimum) "Minimum" else "Maximum"

    response.setContentString(s"${string} target sample is: ${sample}")

    Future.value(
      response
    )
  }

}

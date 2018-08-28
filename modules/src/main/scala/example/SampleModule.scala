package example

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.TwitterModule

object SampleModule extends TwitterModule {
  val showMin = flag("reverse", false, "Reverses the string.")

  @Singleton
  @Provides
  def providesSampleService: SampleService = new SampleService(showMin.apply())

}
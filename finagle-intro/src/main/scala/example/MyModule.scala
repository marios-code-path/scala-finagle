package example

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.TwitterModule

object MyModule extends TwitterModule {
  val showMin = flag("min", false, "Show minimum instead of maximum.")

  @Singleton
  @Provides
  def providesMyService: MyService = {
    new MyService(showMin.apply())
  }

}
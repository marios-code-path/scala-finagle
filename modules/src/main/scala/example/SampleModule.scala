package example

import com.google.inject.{Provider, Provides, Singleton}
import com.twitter.inject.TwitterModule
import org.apache.commons.lang.RandomStringUtils

// This module bundles it's own flag. Its a scoping mechanism we can use to
// isolate specific feature flags per module.
object SampleModule extends TwitterModule {
  val flagReverse = flag("reverse", false, "Reverses the string.")

  @Provides
  def providesRandomString: String = "T" + RandomStringUtils.randomAlphabetic(8)

  @Singleton
  @Provides
  def providesSampleService(prov: Provider[String]): SampleService = new SampleService(flagReverse.apply(), prov)
}
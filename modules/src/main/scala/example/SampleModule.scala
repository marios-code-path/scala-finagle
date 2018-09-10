package example

import com.google.inject.{Provider, Provides, Singleton}
import com.twitter.inject.{Logging, TwitterModule}
import org.apache.commons.lang.RandomStringUtils

trait StringMaker extends (Unit => String)

object SampleModule extends TwitterModule with Logging {
  val flagReverse = flag("reverse", false, "Reverses the string.")

  @Provides
  def providesRandomString: StringMaker = _ => "T" + RandomStringUtils.randomAlphabetic(8)

  @Singleton
  @Provides
  def providesSampleService(prov: Provider[StringMaker]): SampleService = new SampleService(flagReverse.apply(), prov)
}
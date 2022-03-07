package urlshortener

import izumi.reflect.Tag
import urlshortener.AppSpec.AppTestEnv
import zio.logging.Logging
import zio.test.environment.TestEnvironment
import zio.test.mock.Expectation
import zio.test.{DefaultRunnableSpec, ZSpec}
import zio.{Has, UManaged}

object AppSpec {
  type AppTestEnv = TestEnvironment with Logging
}

abstract class AppSpec extends DefaultRunnableSpec {
  def appSpec: ZSpec[AppTestEnv, Any]

  final override def spec: ZSpec[TestEnvironment, Any] = appSpec.provideCustomLayer(Logging.console())

  def managedMock[A: Tag](expectation: Expectation[Has[A]]): UManaged[A] = expectation.toLayer.build.map(_.get)
}

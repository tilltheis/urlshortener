package urlshortener

import urlshortener.AppSpec.AppTestEnv
import urlshortener.model.UrlId
import zio.test.Assertion._
import zio.test._
import zio.test.mock.Expectation._
import zio.test.mock.{MockRandom, mockable}

import java.net.URL

object UrlShortenerServiceImplSpec extends AppSpec {
  @mockable[UrlDao]
  object MockUrlDao

  override def appSpec: ZSpec[AppTestEnv, Any] =
    suite(getClass.getSimpleName)(
      suite("shortenUrl") {
        testM("delegates to the DAO") {
          val managedDaoMock    = managedMock(
            MockUrlDao.StoreUrl(equalTo((UrlId("00000000-0000-4001-8000-000000000002")), new URL("http://example.org")))
          )
          val managedRandomMock = managedMock(MockRandom.NextLong(value(1L)) ++ MockRandom.NextLong(value(2L)))
          managedDaoMock.zip(managedRandomMock).use { case (dao, random) =>
            val service = new UrlShortenerServiceImpl(dao, random)
            assertM(service.shortenUrl(new URL("http://example.org")))(
              equalTo(UrlId("00000000-0000-4001-8000-000000000002"))
            )
          }
        }
      },
      suite("expandUrl") {
        testM("delegates to the DAO") {
          val managedDaoMock = managedMock(
            MockUrlDao.LoadUrl(
              equalTo(UrlId("00000000-0000-4001-8000-000000000002")),
              value(Some(new URL("http://example.org")))
            )
          )
          managedDaoMock.zip(MockRandom.empty.build.map(_.get)).use { case (dao, random) =>
            val service = new UrlShortenerServiceImpl(dao, random)
            assertM(service.expandUrl(UrlId("00000000-0000-4001-8000-000000000002")))(
              isSome(equalTo(new URL("http://example.org")))
            )
          }
        }
      }
    )
}

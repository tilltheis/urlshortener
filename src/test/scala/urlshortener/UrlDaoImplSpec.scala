package urlshortener

import urlshortener.AppSpec.AppTestEnv
import urlshortener.model.UrlId
import zio.ZRef
import zio.test.Assertion._
import zio.test._

import java.net.URL

object UrlDaoImplSpec extends AppSpec {
  override def appSpec: ZSpec[AppTestEnv, Any] =
    suite(getClass.getSimpleName)(
      suite("storeUrl")(testM("stores URL that can be loaded again") {
        for {
          store     <- ZRef.make(Map.empty[UrlId, URL])
          dao        = new UrlDaoImpl(store)
          _         <- dao.storeUrl(UrlId("urlId"), new URL("http://example.org"))
          loadedUrl <- dao.loadUrl(UrlId("urlId"))
        } yield assertTrue(loadedUrl.get == new URL("http://example.org"))
      }),
      suite("loadUrl")(
        testM("returns nothing for unknown IDs") {
          for {
            store          <- ZRef.make(Map.empty[UrlId, URL])
            dao             = new UrlDaoImpl(store)
            maybeLoadedUrl <- dao.loadUrl(UrlId("doesNotExist"))
          } yield assert(maybeLoadedUrl)(isNone)
        },
        test("returns something for known IDs") {
          // this is already tested by the "storeUrl" suite
          assertCompletes
        }
      )
    )
}

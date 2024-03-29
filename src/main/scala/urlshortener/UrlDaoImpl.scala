package urlshortener

import urlshortener.common.AppUIO
import urlshortener.model.UrlId
import zio.Ref

import java.net.URL

class UrlDaoImpl(storeRef: Ref[Map[UrlId, URL]]) extends UrlDao {
  override def loadUrl(urlId: UrlId): AppUIO[Option[URL]] = storeRef.get.map(_.get(urlId))

  override def storeUrl(urlId: UrlId, url: URL): AppUIO[Unit] = storeRef.update(_.updated(urlId, url))
}

package urlshortener

import urlshortener.common.AppUIO
import urlshortener.model.UrlId

import java.net.URL

trait UrlDao {
  def loadUrl(urlId: UrlId): AppUIO[Option[URL]]
  def storeUrl(urlId: UrlId, url: URL): AppUIO[Unit]
}

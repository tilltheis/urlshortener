package urlshortener

import urlshortener.common.AppUIO
import urlshortener.model.UrlId

import java.net.URL

trait UrlShortenerService {
  def shortenUrl(longUrl: URL): AppUIO[UrlId]
  def expandUrl(urlId: UrlId): AppUIO[Option[URL]]
}

package urlshortener

import urlshortener.common.AppUIO
import urlshortener.model.UrlId
import zio.random.Random

import java.net.URL

class UrlShortenerServiceImpl(dao: UrlDao, random: Random.Service) extends UrlShortenerService {
  override def shortenUrl(longUrl: URL): AppUIO[UrlId] = for {
    uuid <- random.nextUUID // use fancy short id algorithm instead
    id    = UrlId(uuid.toString)
    _    <- dao.storeUrl(id, longUrl)
  } yield id

  def expandUrl(urlId: UrlId): AppUIO[Option[URL]] = dao.loadUrl(urlId)
}

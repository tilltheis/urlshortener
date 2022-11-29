package urlshortener

import urlshortener.model.UrlId
import zio.random.Random
import zio.{UManaged, ZRef}

import java.net.URL

final case class UrlShortenerModule(urlShortenerService: UrlShortenerService)

object UrlShortenerModule {
  def managed(random: Random.Service): UManaged[UrlShortenerModule] = for {
    store  <- ZRef.make(Map.empty[UrlId, URL]).toManaged_
    dao     = new UrlDaoImpl(store)
    service = new UrlShortenerServiceImpl(dao, random)
  } yield UrlShortenerModule(service)
}

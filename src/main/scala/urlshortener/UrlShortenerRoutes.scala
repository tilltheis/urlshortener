package urlshortener

import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Uri}
import urlshortener.common.AppTask
import urlshortener.model.UrlId
import zio.ZIO
import zio.interop.catz._

import java.net.URL

class UrlShortenerRoutes(service: UrlShortenerService, baseUri: Uri) {
  private val dsl = Http4sDsl[AppTask]
  import dsl._

  val routes: HttpRoutes[AppTask] = HttpRoutes.of[AppTask] {
    case request @ POST -> Root =>
      for {
        longUrlString <- request.bodyText.compile.string
        response      <- ZIO(new URL(longUrlString)).foldM(
                           _ => BadRequest(),
                           longUrl =>
                             for {
                               urlId    <- service.shortenUrl(longUrl)
                               response <- Ok((baseUri / urlId.value).renderString)
                             } yield response
                         )
      } yield response

    case GET -> Root / urlIdString =>
      for {
        urlId        <- ZIO(UrlId(urlIdString))
        maybeLongUrl <- service.expandUrl(urlId)
        response     <- maybeLongUrl.map(x => Ok(x.toExternalForm)).getOrElse(NotFound())
      } yield response
  }
}

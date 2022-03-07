package urlshortener

import org.http4s.Method.{GET, POST}
import org.http4s.Status.NotFound
import org.http4s.{Status, Uri}
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits.http4sLiteralsSyntax
import urlshortener.AppSpec.AppTestEnv
import urlshortener.common.AppTask
import urlshortener.model.UrlId
import zio.ZIO
import zio.interop.catz._
import zio.test.Assertion._
import zio.test._
import zio.test.mock.Expectation._
import zio.test.mock.mockable

import java.net.URL

object UrlShortenerRoutesSpec extends AppSpec {
  private val dsl = Http4sClientDsl[AppTask]
  import dsl._

  @mockable[UrlShortenerService]
  object MockUrlShortenerService

  val baseUri = uri"http://example.org"

  override def appSpec: ZSpec[AppTestEnv, Any] = suite(getClass.getSimpleName)(
    suite("POST /")(
      testM("with valid url post body creates a new short url ID") {
        val managedServiceMock = managedMock(
          MockUrlShortenerService.ShortenUrl(
            equalTo(new URL("http://example.org/longUrl")),
            value(UrlId("shortUrl"))
          )
        )
        managedServiceMock.use { service =>
          val httpApp        = new UrlShortenerRoutes(service, baseUri).routes.orNotFound
          val request        = POST(baseUri).withEntity("http://example.org/longUrl")
          val responseUrlZio = httpApp.run(request).flatMap(_.as[String])
          assertM(responseUrlZio)(equalTo("http://example.org/shortUrl"))
        }
      },
      testM("with malformed url post body returns 400 Bad Request") {
        MockUrlShortenerService.empty.build.map(_.get).use { service =>
          val httpApp           = new UrlShortenerRoutes(service, baseUri).routes.orNotFound
          val request           = POST(baseUri).withEntity("malformed url")
          val responseStatusZio = httpApp.run(request).map(_.status)
          assertM(responseStatusZio)(equalTo(Status.BadRequest))
        }
      }
    ),
    suite("GET /$urlId")(
      testM("with unknown URL ID returns 404 Not Found") {
        val managedServiceMock = managedMock(
          MockUrlShortenerService.ExpandUrl(equalTo(UrlId("doesNotExist")), value(None))
        )
        managedServiceMock.use { service =>
          val httpApp           = new UrlShortenerRoutes(service, baseUri).routes.orNotFound
          val request           = GET(baseUri / "doesNotExist")
          val responseStatusZio = httpApp.run(request).map(_.status)
          assertM(responseStatusZio)(equalTo(NotFound))
        }
      },
      testM("with valid URL ID returns the URL") {
        val managedServiceMock = managedMock(
          MockUrlShortenerService.ShortenUrl(
            equalTo(new URL("http://example.org/longUrl")),
            value(UrlId("shortUrl"))
          ) ++
            MockUrlShortenerService.ExpandUrl(
              equalTo(UrlId("shortUrl")),
              value(Some(new URL("http://example.org/longUrl")))
            )
        )
        managedServiceMock.use { service =>
          val httpApp = new UrlShortenerRoutes(service, baseUri).routes.orNotFound
          for {
            createRequest <- ZIO.succeed(POST(baseUri).withEntity("http://example.org/longUrl"))
            longUrlString <- httpApp.run(createRequest).flatMap(_.as[String])
            longUrl       <- ZIO.fromEither(Uri.fromString(longUrlString))
            lookupRequest <- ZIO.succeed(GET(longUrl))
            responseUrl   <- httpApp.run(lookupRequest).flatMap(_.as[String])
          } yield assertTrue(responseUrl == "http://example.org/longUrl")
        }
      }
    )
  )
}

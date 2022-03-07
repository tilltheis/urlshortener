package urlshortener

import org.http4s.Method.{GET, POST}
import org.http4s.Uri
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits.http4sLiteralsSyntax
import urlshortener.AppSpec.AppTestEnv
import urlshortener.common.AppTask
import zio.interop.catz._
import zio.test._
import zio.{ZEnv, ZIO}

object AppIntegrationSpec extends AppSpec {
  private val dsl = Http4sClientDsl[AppTask]
  import dsl._

  private val baseUrl = uri"http://localhost:8080"

  override def appSpec: ZSpec[AppTestEnv, Any] = suite("AppIntegrationSpec")(
    testM("shorten and expand URL") {
      ZIO.runtime[ZEnv].flatMap { implicit runtime =>
        BlazeClientBuilder[AppTask].resource.toManagedZIO.use { client =>
          for {
            createRequest  <- ZIO.succeed(POST(baseUrl).withEntity("http://example.org"))
            shortUrlString <- client.expect[String](createRequest)
            shortUrl       <- ZIO.fromEither(Uri.fromString(shortUrlString))
            lookupRequest  <- ZIO.succeed(GET(shortUrl))
            longUrlString  <- client.expect[String](lookupRequest)
          } yield assertTrue(longUrlString == "http://example.org")
        }
      }
    }
  ) @@ TestAspect.aroundAll(App.appManaged.preallocate)(_.useNow)
}

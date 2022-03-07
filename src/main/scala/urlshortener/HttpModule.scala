package urlshortener

import cats.data.OptionT
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.server.Server
import org.http4s.server.middleware.Logger
import urlshortener.common.AppTask
import zio.interop.catz._
import zio.logging.Logging
import zio.{RManaged, ZEnv, ZManaged}

final case class HttpModule(server: Server)

object HttpModule extends (Server => HttpModule) {

  def managed(urlShortenerModule: UrlShortenerModule): RManaged[ZEnv with Logging, HttpModule] = {
    val routes = new UrlShortenerRoutes(urlShortenerModule.urlShortenerService, uri"http://localhost:8080")
    ZManaged.runtime[ZEnv].flatMap { implicit runtime =>
      // if we don't resurrect or absorb then a died fiber results in no http4s response at all
      val resurrectedRoutes = routes.routes.mapF { case OptionT(task) => OptionT(task.absorb) }
      val httpApp           = Logger.httpApp(logHeaders = false, logBody = false)(resurrectedRoutes.orNotFound)
      BlazeServerBuilder[AppTask]
        .withExecutionContext(runtime.platform.executor.asEC)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(httpApp)
        .resource
        .toManagedZIO
        .map(HttpModule)
    }
  }

}

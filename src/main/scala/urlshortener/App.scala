package urlshortener

import zio.logging.slf4j.Slf4jLogger
import zio.logging.{Logging, log}
import zio.random.Random
import zio.{App, ExitCode, RManaged, URIO, ZEnv, ZManaged}

object App extends App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    appManaged.provideCustomLayer(Slf4jLogger.make((_, message) => message)).useForever.exitCode

  def appManaged: RManaged[ZEnv with Logging, Unit] = for {
    random             <- ZManaged.service[Random.Service]
    urlShortenerModule <- UrlShortenerModule.managed(random)
    _                  <- HttpModule.managed(urlShortenerModule)
    _                  <- log.info("hello world").toManaged_
  } yield ()
}

package urlshortener

import zio.{RIO, URIO}
import zio.logging.Logging

object common {
  type AppUIO[A]  = URIO[Logging, A]
  type AppTask[A] = RIO[Logging, A]
}

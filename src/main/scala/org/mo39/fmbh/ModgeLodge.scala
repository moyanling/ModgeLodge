package org.mo39.fmbh
import almond.api.helpers.Display
import almond.interpreter.api.OutputHandler
import com.typesafe.scalalogging.LazyLogging

object ModgeLodge extends App with LazyLogging {

  logger.info("Hello, World!")

  def Greetings()(implicit o: OutputHandler): Display =
    Display.html(<h1>Hello, World!</h1>.toString())

}

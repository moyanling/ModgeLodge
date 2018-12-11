package org.mo39.fmbh
import com.typesafe.scalalogging.LazyLogging

import sys.process._
import scala.language.postfixOps

object ModgeLodge extends App with LazyLogging {

  case class Config(start: Boolean = false)

//  val parser = new scopt.OptionParser[Config]("scopt") {
//    head("ModgeLodge", "0.1")
//
//    opt[Unit]("start").abbr("s").text("start Jupyter Notebook")
//
//    help("help").text("prints this usage text")
//  }

  def Greetings(): String = "<h1>Hello, World!</h1>"

  logger.info("Greetings.")

//  parser.parse(args, Config()) match {
//    case Some(config) => if (config.start) "jupyter notebook" !!
//
//    case None =>
//    // arguments are bad, error message will have been displayed
//  }

}

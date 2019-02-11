package org.mo39.fmbh.common.app

import com.typesafe.scalalogging.LazyLogging

import scala.language.dynamics

trait ZeppelinMLApp extends App with LazyLogging {

  /* util to display the feature, label etc. */
  object display extends Dynamic {

    val app: ZeppelinMLApp = ZeppelinMLApp.this

    def applyDynamic(target: String)(args: AnyRef*): Unit = {
      val method = target + "ToXml"
      val xml = args.length match {
        case 1 =>
          classOf[ZeppelinMLApp]
            .getMethod(method, classOf[AnyRef])
            .invoke(app, args(0))
            .toString
      }
      println(s"%html $xml")
    }

  }

}

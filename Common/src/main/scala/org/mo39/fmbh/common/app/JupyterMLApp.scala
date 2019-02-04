package org.mo39.fmbh.common.app

import almond.api.helpers.{ Display => AlmondDisplay }
import almond.interpreter.api.OutputHandler
import com.typesafe.scalalogging.LazyLogging

trait JupyterMLApp extends App with Displayable with LazyLogging {

  /* util to display the feature, label etc. */
  //TODO Make display members dynamic and support list method
  object display {

    val displayable: Displayable = JupyterMLApp.this

    implicit def xmlToDisplay(x: xml.Elem)(implicit o: OutputHandler): AlmondDisplay =
      AlmondDisplay.html(x.toString())

    /* Display a given Feature */
    def feature(a: Any)(implicit o: OutputHandler): AlmondDisplay = displayable.featureToXml(a)
    /* Display a given Label */
    def label(a: Any)(implicit o: OutputHandler): AlmondDisplay = displayable.labelToXml(a)
    /* Display a given Config */
    def config(a: Any)(implicit o: OutputHandler): AlmondDisplay = displayable.configToXml(a)
  }

}

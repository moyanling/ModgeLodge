package org.mo39.fmbh.common
import almond.api.helpers.Display
import almond.interpreter.api.OutputHandler
import org.mo39.fmbh.common.ml.Config
import org.mo39.fmbh.common.util.Reflect

trait JupyterMLApp extends App {

  implicit def xmlToDisplay(x: xml.Elem)(implicit o: OutputHandler): Display =
    Display.html(x.toString())

  /* Inspect the configuration */
  def inspect(a: Any)(implicit o: OutputHandler): Display = configToXml(a)
  /* View a given Feature */
  def viewFeature(a: Any)(implicit o: OutputHandler): Display = featureToXml(a)
  /* View a given Label */
  def viewLabel(a: Any)(implicit o: OutputHandler): Display = labelToXml(a)

  def featureToXml(a: Any): xml.Elem
  def labelToXml(a: Any): xml.Elem
  def configToXml(a: Any): xml.Elem = {
    val metadata = Reflect.getCaseAccessorMetaData[Config](a)
    if (metadata.isEmpty) return <table></table>
    val clazz = a.getClass
    val headers = "name" :: "value" :: metadata.head._2.map(_._1)
    val rows: List[List[String]] = metadata.map { row =>
      val name = row._1
      val value = clazz.getMethod(name).invoke(a).toString
      name :: value :: row._2.map(_._2)
    }
    <table style="width:100%;table-layout:auto;" >
      <tr>
        {headers.map(h => <th>{h}</th>)}
      </tr>
      {
      rows.map { row =>
        <tr>
          { row.map(col => <td>{col}</td>) }
        </tr>
      }
      }
    </table>
  }

}


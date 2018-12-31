package org.mo39.fmbh.common.util
import almond.api.helpers.Display
import almond.interpreter.api.OutputHandler
import org.mo39.fmbh.common.ml.Config

trait JupyterDisplay {

  implicit def xmlToDisplay(x: xml.Elem)(implicit o: OutputHandler): Display =
    Display.html(x.toString())

  /* Inspect the configuration */
  def inspect()(implicit o: OutputHandler): Display = configToXml()
  /* View a given Feature */
  def viewFeature(a: Any)(implicit o: OutputHandler): Display = featureToXml(a)
  /* View a given Label */
  def viewLabel(a: Any)(implicit o: OutputHandler): Display = labelToXml(a)

  def featureToXml(a: Any): xml.Elem
  def labelToXml(a: Any): xml.Elem
  def configToXml(): xml.Elem = {
    val metadata = Reflect.getCaseAccessorMetaData[Config](this)
    if (metadata.isEmpty) return <table></table>
    val clazz = getClass
    val headers = "name" :: "value" :: metadata.head._2.map(_._1)
    val rows: List[List[String]] = metadata.map { row =>
      val name = row._1
      val value = clazz.getMethod(name).invoke(this).toString
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

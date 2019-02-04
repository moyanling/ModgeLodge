package org.mo39.fmbh.common.app
import org.mo39.fmbh.common.ml.config
import org.mo39.fmbh.common.util.Reflect

/**
  * This trait display should be a powerful tool to display
  * the xml as html to help visualize in Jupyter Notebook
  *
  * @author mo39.fmbh
  */
trait Displayable {

  def featureToXml(a: Any): xml.Elem
  def labelToXml(a: Any): xml.Elem
  def configToXml(a: Any): xml.Elem = {
    val metadata = Reflect.getCaseAccessorMetaData[config](a)
    if (metadata.isEmpty) return <p>No config found.</p>
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

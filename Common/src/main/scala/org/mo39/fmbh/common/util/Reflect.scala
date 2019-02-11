package org.mo39.fmbh.common.util

import scala.reflect.runtime.universe._

object Reflect {

  /** Get the meta data, annotated with the TypeTag, for the case accessors of a case class
    *
    * @param any The case class
    * @tparam T The TypeTag of the specified Annotation
    * @return A list of case accessors where each is associated with a list of properties
    */
  def getCaseAccessorMetaData[T: TypeTag](any: AnyRef): List[(String, List[(String, String)])] = {
    val clazz = any.getClass
    val ofTypeT: Annotation => Boolean = (a: Annotation) => a.tree.tpe =:= typeOf[T]
    /* Find the case accessors that are annotated with T and its properties */
    runtimeMirror(clazz.getClassLoader)
      .classSymbol(clazz)
      .toType
      .member(termNames.CONSTRUCTOR)
      .asMethod
      .paramLists
      .head
      .filter(_.annotations.exists(ofTypeT))
      .map(ap => (ap.name.toString, propertiesOf(ap.annotations.find(ofTypeT).get)))
  }

  /* Find a list of properties for an annotation instance */
  def propertiesOf(annotation: Annotation): List[(String, String)] =
    annotation.tree.children
      .filter(_.isInstanceOf[AssignOrNamedArg])
      .map(_.asInstanceOf[AssignOrNamedArg])
      .map(t => (t.lhs.toString(), t.rhs.productElement(0).asInstanceOf[Constant].value.toString))

  /* Get the type name of an annotation */
  def typeNameOf(annotation: Annotation): String = annotation.tree.tpe.typeSymbol.fullName.toString

}

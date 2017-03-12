package de.codepitbull.vertx.scala.ddd.vertx

package object kryo {
  def isCaseClass(v: Class[_]): Boolean = {
    import reflect.runtime.universe._
    runtimeMirror(v.getClass.getClassLoader).classSymbol(v).isCaseClass
  }

    def validateCaseClasses(clazzes: Seq[Class[_]]): Unit = {
    val nonCaseClasses = clazzes.filter(!isCaseClass(_))
    if (nonCaseClasses.nonEmpty)
      throw new IllegalArgumentException(s"The following classes aren't case classes: ${nonCaseClasses.mkString("/")}")
  }

}

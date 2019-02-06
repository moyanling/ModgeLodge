package org.mo39.fmbh.common
import java.nio.file.{ Path, Paths }


object Env {

  /* Current working directory */
  val pwd: Path = Paths.get(System.getProperty("user.dir"))
  /* User home directory */
  val userHome: Path = Paths.get(System.getProperty("user.home"))
  /* Model path */
  val models: Path = pwd.resolve("models")
  /* Notebook path */
  val notebooks: Path = pwd.resolve("notebooks")
  /* User local Ivy repo path */
  val userRepo: Path = userHome.resolve(".ivy2")

}

package org.mo39.fmbh.common.sbt

import sys.process._
import scala.util.{ Failure, Success, Try }

case class ZeppelinTask(
    name: String = "",
    version: String = "",
    logOnSuccess: String => Unit = _ => Unit,
    logOnFailure: String => Unit = _ => Unit
) {

  val image = "apache/zeppelin"
  val tag = "0.8.1"
  val mountLogs = "-v $PWD/logs:/logs"
  val mountMaven = "-v $PWD/notebook:/notebook"
  val mountNotebook = "-v $PWD/notebook:/notebook"
  val runContainerCmd = s"docker run -p 8080:8080 $mountLogs $mountMaven $mountNotebook $image:$tag"

  def run(): Unit = {
    runContainerCmd.!!
  }

  def clean(): Unit = {
    _run(stopRunningContainers)
    _run(removeAllContainers)
    _run(removeUntaggedImages)
  }

  private[this] val _run: CmdChain => Unit = { cmdChain =>
    val result = cmdChain.cmd1.!!
    /* Check if the result of the first command is empty */
    if (result.nonEmpty) {
      Try(s"${cmdChain.cmd2} $result".!!) match {
        case Success(_) => logOnSuccess(cmdChain.successMsg)
        case Failure(e) => logOnFailure(e.getMessage)
      }
    }
  }

  val stopRunningContainers = CmdChain(
    cmd1 = "docker ps -q",
    cmd2 = "docker kill",
    successMsg = "Stopped running containers"
  )
  val removeAllContainers = CmdChain(
    cmd1 = "docker ps -aq",
    cmd2 = "docker rm",
    successMsg = "Removed all containers"
  )
  val removeUntaggedImages = CmdChain(
    cmd1 = """docker images -q --filter "dangling=true"""",
    cmd2 = "docker rmi",
    successMsg = "Removed all untagged images"
  )

}

case class CmdChain(cmd1: String, cmd2: String, successMsg: String)

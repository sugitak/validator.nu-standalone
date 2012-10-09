package org.w3.htmlvalidator

object HTMLValidatorMain {

  def main(args: Array[String]): Unit = {
    val port = args.toList.headOption.map(_.toInt) getOrElse 8888
    val conf = HTMLValidatorConfiguration.default
    conf.setSystemProperties()

    val htmlval = HTMLValidator(port)

    htmlval.start()

    println(">> press Enter to stop")
    scala.Console.readLine()

    htmlval.stop()
  }
}

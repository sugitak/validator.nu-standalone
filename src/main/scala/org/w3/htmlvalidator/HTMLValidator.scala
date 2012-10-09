package org.w3.htmlvalidator

import nu.validator.servlet
import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import nu.validator.servletfilter.InboundGzipFilter
import nu.validator.servletfilter.InboundSizeLimitFilter
import org.apache.log4j.PropertyConfigurator
import org.mortbay.jetty.Connector
import org.mortbay.jetty.Handler
import org.mortbay.jetty.Server
import org.mortbay.jetty.ajp.Ajp13SocketConnector
import org.mortbay.jetty.bio.SocketConnector
import org.mortbay.jetty.servlet.Context
import org.mortbay.jetty.servlet.FilterHolder
import org.mortbay.jetty.servlet.ServletHolder
import org.mortbay.servlet.GzipFilter
import org.mortbay.thread.QueuedThreadPool
import java.nio.file.Paths
import nu.validator.servlet.MultipartFormDataFilter
import nu.validator.servlet.VerifierServlet
import scala.collection.immutable.Map

object HTMLValidator {

  def apply(port: Int): HTMLValidator = {
    val server: Server = {
      val server = new Server
      val pool: QueuedThreadPool = new QueuedThreadPool
      pool.setMaxThreads(100)
      server.setThreadPool(pool)
      server
    }
    new HTMLValidator(server, port)
  }

}

/**
 * Validator.nu wrapping class
 * @author Hirotaka Nakajima <hiro@w3.org>
 */
class HTMLValidator(server: Server, port: Int, ajp: Boolean = false) {
  // TODO shouldn't this be either passed?
  private val SIZE_LIMIT: Long = Integer.parseInt(System.getProperty("nu.validator.servlet.max-file-size", "2097152"))

  //  if (!"1".equals(System.getProperty("nu.validator.servlet.read-local-log4j-properties"))) {
  PropertyConfigurator.configure(classOf[nu.validator.servlet.Main].getClassLoader().getResource("nu/validator/localentities/files/log4j.properties"))
  //  } else {
  //    PropertyConfigurator.configure(System.getProperty("nu.validator.servlet.log4j-properties", "log4j.properties"))
  //  }

  val connector: Connector =
    if (ajp) {
      val connector = new Ajp13SocketConnector
      connector.setPort(port)
      connector.setHost("127.0.0.1")
      connector
    } else {
      val connector = new SocketConnector
      connector.setPort(port)
      connector
    }

  server.addConnector(connector)

  val context: Context = {
    val context = new Context(server, "/")
    context.addFilter(new FilterHolder(new GzipFilter), "/*", Handler.REQUEST)
    context.addFilter(new FilterHolder(new InboundSizeLimitFilter(SIZE_LIMIT)), "/*", Handler.REQUEST)
    context.addFilter(new FilterHolder(new InboundGzipFilter), "/*", Handler.REQUEST)
    context.addFilter(new FilterHolder(new MultipartFormDataFilter), "/*", Handler.REQUEST)
    context.addServlet(new ServletHolder(new VerifierServlet), "/*")
    context
  }

  def stop(): Unit = {
    // TODO if passed from the outside, the server should not be stopped
    server.stop()
  }

  def start(): Unit = {
    server.start()
  }
}

object HTMLValidatorConfiguration {

  val default: HTMLValidatorConfiguration = {
    val config = Map[String, Any](
      ("nu.validator.servlet.read-local-log4j-properties" -> 1),
      ("nu.validator.servlet.log4j-properties" -> "validator/log4j.properties"),
      ("nu.validator.servlet.version" -> 3),
      ("nu.validator.servlet.service-name" -> "Validator.nu"),
      ("org.whattf.datatype.warn" -> true),
      ("nu.validator.servlet.about-page" -> "http://about.validator.nu/"),
      ("nu.validator.servlet.style-sheet" -> "style.css"),
      ("nu.validator.servlet.icon" -> "icon.png"),
      ("nu.validator.servlet.script" -> "script.js"),
      ("nu.validator.spec.html5-load" -> "http://www.whatwg.org/specs/web-apps/current-work/"),
      ("nu.validator.spec.html5-link" -> "http://www.whatwg.org/specs/web-apps/current-work/"),
      ("nu.validator.servlet.max-file-size" -> 7340032),
      ("nu.validator.servlet.connection-timeout" -> 5000),
      ("nu.validator.servlet.socket-timeout" -> 5000),
      ("nu.validator.servlet.w3cbranding" -> 0),
      ("nu.validator.servlet.statistics" -> 0),
      ("org.mortbay.http.HttpRequest.maxFormContentSize" -> 7340032),
      ("nu.validator.servlet.host.generic" -> ""),
      ("nu.validator.servlet.host.html5" -> ""),
      ("nu.validator.servlet.host.parsetree" -> ""),
      ("nu.validator.servlet.path.generic" -> "/"),
      ("nu.validator.servlet.path.html5" -> "/html5/"),
      ("nu.validator.servlet.path.parsetree" -> "/parsetree/"),
      ("nu.validator.servlet.path.about" -> "./validator/site/"))
    HTMLValidatorConfiguration(config)
  }

}

case class HTMLValidatorConfiguration(config: Map[String, Any]) {

  def readLocalLog4JProperties(b: Boolean): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.read-local-log4j-properties" -> (if (b) 1 else 0)))
  }

  def log4jProperties(s: String): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.log4j-properties" -> s))
  }

  def version(i: Int): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.version" -> i))
  }

  def serviceName(s: String): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.service-name" -> s))
  }

  def dataTypeWarn(b: Boolean): HTMLValidatorConfiguration = {
    copy(config = config + ("org.whattf.datatype.warn" -> b))
  }

  def aboutPage(s: String): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.about-page" -> s))
  }

  def styleSheet(s: String): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.style-sheet" -> s))
  }

  def icon(s: String): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.icon" -> s))
  }

  def script(s: String): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.script" -> s))
  }

  def specHtml5Load(s: String): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.spec.html5-load" -> s))
  }

  def specHtml5Link(s: String): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.spec.html5-link" -> s))
  }

  def maxFileSize(i: Int): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.max-file-size" -> i))
  }

  def connectionTimeout(i: Int): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.connection-timeout" -> i))
  }

  def socketTimeout(i: Int): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.socket-timeout" -> i))
  }

  def w3cBranding(b: Boolean): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.w3cbranding" -> (if (b) 1 else 0)))
  }

  def statistics(b: Boolean): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.statistics" -> (if (b) 1 else 0)))
  }

  def httpRequestMaxFormContentSize(i: Int): HTMLValidatorConfiguration = {
    copy(config = config + ("org.mortbay.http.HttpRequest.maxFormContentSize" -> i))
  }

  def hostGeneric(s: String): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.host.generic" -> s))
  }

  def hostHtml5(s: String): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.host.html5" -> s))
  }

  def hostParseTree(s: String): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.host.parsetree" -> s))
  }

  def pathGeneric(s: String): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.path.generic" -> s))
  }

  def pathHtml5(s: String): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.path.html5" -> s))
  }

  def pathParseTree(s: String): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.path.parsetree" -> s))
  }

  def pathAbout(s: String): HTMLValidatorConfiguration = {
    copy(config = config + ("nu.validator.servlet.path.about" -> s))
  }

  def setSystemProperties(): Unit = {
    config foreach { case (k, v) =>
      System.setProperty(k, v.toString())
    }
  }

}

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

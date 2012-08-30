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

/**
 * Validator.nu wrapping class
 * @author Hirotaka Nakajima <hiro@w3.org>
 */
class HTMLValidator(server: Server, port: Int, ajp: Boolean = false) {
  private val SIZE_LIMIT: Long = Integer.parseInt(System.getProperty("nu.validator.servlet.max-file-size", "2097152"));

  //  if (!"1".equals(System.getProperty("nu.validator.servlet.read-local-log4j-properties"))) {
  PropertyConfigurator.configure(classOf[nu.validator.servlet.Main].getClassLoader().getResource("nu/validator/localentities/files/log4j.properties"));
  //  } else {
  //    PropertyConfigurator.configure(System.getProperty("nu.validator.servlet.log4j-properties", "log4j.properties"));
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

  server.addConnector(connector);

  var context: Context = new Context(server, "/");
  context.addFilter(new FilterHolder(new GzipFilter), "/*", Handler.REQUEST);
  context.addFilter(new FilterHolder(new InboundSizeLimitFilter(SIZE_LIMIT)), "/*", Handler.REQUEST);
  context.addFilter(new FilterHolder(new InboundGzipFilter), "/*", Handler.REQUEST);
  context.addFilter(new FilterHolder(new MultipartFormDataFilter), "/*", Handler.REQUEST);
  context.addServlet(new ServletHolder(new VerifierServlet), "/*");

  def stop(): Unit = {
    server.stop()
  }

  def start(): Unit = {
    server.start()
  }
}

case class HTMLValidatorConfiguration() {

  var configs = Map[String, Any]().empty +
    ("nu.validator.servlet.read-local-log4j-properties" -> 1) +
    ("nu.validator.servlet.log4j-properties" -> "validator/log4j.properties") +
    ("nu.validator.servlet.version" -> 3) +
    ("nu.validator.servlet.service-name" -> "Validator.nu") +
    ("org.whattf.datatype.warn" -> true) +
    ("nu.validator.servlet.about-page" -> "http://about.validator.nu/") +
    ("nu.validator.servlet.style-sheet" -> "style.css") +
    ("nu.validator.servlet.icon" -> "icon.png") +
    ("nu.validator.servlet.script" -> "script.js") +
    ("nu.validator.spec.html5-load" -> "http://www.whatwg.org/specs/web-apps/current-work/") +
    ("nu.validator.spec.html5-link" -> "http://www.whatwg.org/specs/web-apps/current-work/") +
    ("nu.validator.servlet.max-file-size" -> 7340032) +
    ("nu.validator.servlet.connection-timeout" -> 5000) +
    ("nu.validator.servlet.socket-timeout" -> 5000) +
    ("nu.validator.servlet.w3cbranding" -> 0) +
    ("nu.validator.servlet.statistics" -> 0) +
    ("org.mortbay.http.HttpRequest.maxFormContentSize" -> 7340032) +
    ("nu.validator.servlet.host.generic" -> "") +
    ("nu.validator.servlet.host.html5" -> "") +
    ("nu.validator.servlet.host.parsetree" -> "") +
    ("nu.validator.servlet.path.generic" -> "/") +
    ("nu.validator.servlet.path.html5" -> "/html5/") +
    ("nu.validator.servlet.path.parsetree" -> "/parsetree/") +
    ("nu.validator.servlet.path.about" -> "./validator/site/")

  def readLocalLog4JProperties(b: Boolean) = {
    configs = configs + ("nu.validator.servlet.read-local-log4j-properties" -> (if (b) 1 else 0))
  }

  def log4jProperties(s: String) {
    configs = configs + ("nu.validator.servlet.log4j-properties" -> s)
  }

  def version(i: Int) {
    configs = configs + ("nu.validator.servlet.version" -> i)
  }

  def serviceName(s: String) {
    configs = configs + ("nu.validator.servlet.service-name" -> s)
  }

  def dataTypeWarn(b: Boolean) {
    configs = configs + ("org.whattf.datatype.warn" -> b)
  }

  def aboutPage(s: String) {
    configs = configs + ("nu.validator.servlet.about-page" -> s)
  }

  def styleSheet(s: String) {
    configs = configs + ("nu.validator.servlet.style-sheet" -> s)
  }

  def icon(s: String) {
    configs = configs + ("nu.validator.servlet.icon" -> s)
  }

  def script(s: String) {
    configs = configs + ("nu.validator.servlet.script" -> s)
  }

  def specHtml5Load(s: String) {
    configs = configs + ("nu.validator.spec.html5-load" -> s)
  }

  def specHtml5Link(s: String) {
    configs = configs + ("nu.validator.spec.html5-link" -> s)
  }

  def maxFileSize(i: Int) {
    configs = configs + ("nu.validator.servlet.max-file-size" -> i)
  }

  def connectionTimeout(i: Int) {
    configs = configs + ("nu.validator.servlet.connection-timeout" -> i)
  }

  def socketTimeout(i: Int) {
    configs = configs + ("nu.validator.servlet.socket-timeout" -> i)
  }

  def w3cBranding(b: Boolean) {
    configs = configs + ("nu.validator.servlet.w3cbranding" -> (if (b) 1 else 0))
  }

  def statistics(b: Boolean) {
    configs = configs + ("nu.validator.servlet.statistics" -> (if (b) 1 else 0))
  }

  def httpRequestMaxFormContentSize(i: Int) {
    configs = configs + ("org.mortbay.http.HttpRequest.maxFormContentSize" -> i)
  }

  def hostGeneric(s: String) {
    configs = configs + ("nu.validator.servlet.host.generic" -> s)
  }

  def hostHtml5(s: String) {
    configs = configs + ("nu.validator.servlet.host.html5" -> s)
  }

  def hostParseTree(s: String) {
    configs = configs + ("nu.validator.servlet.host.parsetree" -> s)
  }

  def pathGeneric(s: String) {
    configs = configs + ("nu.validator.servlet.path.generic" -> s)
  }

  def pathHtml5(s: String) {
    configs = configs + ("nu.validator.servlet.path.html5" -> s)
  }

  def pathParseTree(s: String) {
    configs = configs + ("nu.validator.servlet.path.parsetree" -> s)
  }

  def pathAbout(s: String) {
    configs = configs + ("nu.validator.servlet.path.about" -> s)
  }

  def setSystemProperties = {
    configs.foreach {
      case (k, v) => {
        System.setProperty(k, v.toString())
      }
    }
  }

}

object HTMLValidatorMain {

  def main(args: Array[String]): Unit = {
    val port = args.toList.headOption.map(_.toInt) getOrElse 8888
    val valconf = HTMLValidatorConfiguration()
    valconf.setSystemProperties
    var server: Server = new Server
    var pool: QueuedThreadPool = new QueuedThreadPool
    pool.setMaxThreads(100);
    server.setThreadPool(pool);

    val htmlval = new HTMLValidator(server, port)

    htmlval.start()

    println(">> press Enter to stop")
    scala.Console.readLine()

    htmlval.stop()
  }
}
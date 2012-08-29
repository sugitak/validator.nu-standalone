package org.w3.htmlvalidator

import nu.validator.servlet
import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import nu.validator.servlet.Main
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
  PropertyConfigurator.configure(classOf[Main].getClassLoader().getResource("nu/validator/localentities/files/log4j.properties"));
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

class HTMLValidatorConfiguration(
  val readLocalLog4JProperties: Int = 1,
  val log4jProperties: String = "validator/log4j.properties",
  val version: Int = 3,
  val serviceName: String = "Validator.nu",
  val dataTypeWarn: Boolean = true,
  val aboutPage: String = "http://about.validator.nu/",
  val styleSheet: String = "style.css",
  val icon: String = "icon.png",
  val script: String = "script.js",
  val specHtml5Load: String = "http://www.whatwg.org/specs/web-apps/current-work/",
  val specHtml5Link: String = "http://www.whatwg.org/specs/web-apps/current-work/",
  val maxFileSize: Int = 7340032,
  val connectionTimeout: Int = 5000,
  val socketTimeout: Int = 5000,
  val w3cBranding: Int = 0,
  val statistics: Int = 0,
  val httpRequestMaxFormContentSize: Int = 7340032,
  val hostGeneric: String = "",
  val hostHtml5: String = "",
  val hostParseTree: String = "",
  val pathGeneric: String = "/",
  val pathHtml5: String = "/html5/",
  val pathParseTree: String = "/parsetree/",
  val pathAbout: String = "./validator/site/") {

  case class ValidatorConfig(confname: String, value: Any)

  implicit def triple2config(triple: (String, Any)): ValidatorConfig = triple match {
    case (confname, value) => ValidatorConfig(confname, value)
  }

  val configs: List[ValidatorConfig] = List(
    ("nu.validator.servlet.read-local-log4j-properties", readLocalLog4JProperties),
    ("nu.validator.servlet.log4j-properties", log4jProperties),
    ("nu.validator.servlet.version", version),
    ("nu.validator.servlet.service-name", serviceName),
    ("org.whattf.datatype.warn", dataTypeWarn),
    ("nu.validator.servlet.about-page", aboutPage),
    ("nu.validator.servlet.style-sheet", styleSheet),
    ("nu.validator.servlet.icon", icon),
    ("nu.validator.servlet.script=script", script),
    ("nu.validator.spec.html5-load", specHtml5Load),
    ("nu.validator.spec.html5-link", specHtml5Link),
    ("nu.validator.servlet.max-file-size", maxFileSize),
    ("nu.validator.servlet.connection-timeout", connectionTimeout),
    ("nu.validator.servlet.socket-timeout", socketTimeout),
    ("nu.validator.servlet.w3cbranding", w3cBranding),
    ("nu.validator.servlet.statistics", statistics),
    ("org.mortbay.http.HttpRequest.maxFormContentSize", httpRequestMaxFormContentSize),
    ("nu.validator.servlet.host.generic", hostGeneric),
    ("nu.validator.servlet.host.html5", hostHtml5),
    ("nu.validator.servlet.host.parsetree", hostParseTree),
    ("nu.validator.servlet.path.generic", pathGeneric),
    ("nu.validator.servlet.path.html5", pathHtml5),
    ("nu.validator.servlet.path.parsetree", pathParseTree),
    ("nu.validator.servlet.path.about", pathAbout))

  def setSystemProperties = {
    configs map {
      case i => {
        System.setProperty(i.confname, i.value.toString())
      }
    }
  }

}

object HTMLValidatorMain {

  def main(args: Array[String]): Unit = {
    val port = args.toList.headOption.map(_.toInt) getOrElse 8888
    val valconf = new HTMLValidatorConfiguration()
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
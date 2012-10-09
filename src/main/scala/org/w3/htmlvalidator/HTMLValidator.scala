package org.w3.htmlvalidator

import java.io.{ InputStream, OutputStream }
import java.net.{ ConnectException, InetAddress, ServerSocket, Socket }
import nu.validator.servletfilter.{ InboundGzipFilter, InboundSizeLimitFilter }
import org.apache.log4j.PropertyConfigurator
import org.mortbay.jetty.{ Connector, Handler, Server }
import org.mortbay.jetty.ajp.Ajp13SocketConnector
import org.mortbay.jetty.bio.SocketConnector
import org.mortbay.jetty.servlet.{ Context, FilterHolder, ServletHolder }
import org.mortbay.servlet.GzipFilter
import org.mortbay.thread.QueuedThreadPool
import java.nio.file.Paths
import nu.validator.servlet.{ MultipartFormDataFilter, VerifierServlet }

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

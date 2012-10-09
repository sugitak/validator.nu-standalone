package org.w3.htmlvalidator

import nu.validator.servletfilter.{ InboundGzipFilter, InboundSizeLimitFilter }
import org.apache.log4j.PropertyConfigurator
import org.eclipse.jetty.server.{ Server, Handler }
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.servlet.{ ServletContextHandler, ServletHolder, FilterHolder }
import org.eclipse.jetty.servlets.GzipFilter
import org.eclipse.jetty.util.thread.QueuedThreadPool
import nu.validator.servlet.{ MultipartFormDataFilter, VerifierServlet }
import javax.servlet.DispatcherType
import java.util.EnumSet

object HTMLValidator {

  def apply(port: Int): HTMLValidator = {
    val server: Server = {
      val connector = {
        val connector = new SelectChannelConnector
        connector.setPort(port)
        connector.setMaxIdleTime(90000)
        connector
      }
      val pool: QueuedThreadPool = {
        val pool = new QueuedThreadPool
        pool.setMaxThreads(100)
        pool
      }
      val server = {
        val server = new Server
        server.setGracefulShutdown(500)
        server.setSendServerVersion(false)
        server.setSendDateHeader(true)
        server.setStopAtShutdown(true)
        server.setThreadPool(pool)
        server.addConnector(connector)
        server
      }
      server
    }
    new HTMLValidator(server, port)
  }

}

/**
 * Validator.nu wrapping class
 * @author Hirotaka Nakajima <hiro@w3.org>
 */
class HTMLValidator(server: Server, port: Int) {
  // TODO shouldn't this be either passed?
  private val SIZE_LIMIT: Long = Integer.parseInt(System.getProperty("nu.validator.servlet.max-file-size", "2097152"))

  //  if (!"1".equals(System.getProperty("nu.validator.servlet.read-local-log4j-properties"))) {
  PropertyConfigurator.configure(classOf[nu.validator.servlet.Main].getClassLoader().getResource("nu/validator/localentities/files/log4j.properties"))
  //  } else {
  //    PropertyConfigurator.configure(System.getProperty("nu.validator.servlet.log4j-properties", "log4j.properties"))
  //  }

  val context: ServletContextHandler = {
    val context = new ServletContextHandler(server, "/")
    val dispatches = EnumSet.of(DispatcherType.REQUEST)
    context.addFilter(new FilterHolder(new GzipFilter), "/*", dispatches)
    context.addFilter(new FilterHolder(new InboundSizeLimitFilter(SIZE_LIMIT)), "/*", dispatches)
    context.addFilter(new FilterHolder(new InboundGzipFilter), "/*", dispatches)
    context.addFilter(new FilterHolder(new MultipartFormDataFilter), "/*", dispatches)
    context.addServlet(new ServletHolder(new VerifierServlet), "/*")
    context
  }

  server.setHandler(context)

  def stop(): Unit = {
    // TODO if passed from the outside, the server should not be stopped
    server.stop()
  }

  def start(): Unit = {
    server.start()
  }
}

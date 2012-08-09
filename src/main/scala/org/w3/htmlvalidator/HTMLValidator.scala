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

/**
 * Validator.nu wrapping class
 * @author Hirotaka Nakajima <hiro@w3.org>
 */
class HTMLValidator(port: Int, ajp:Boolean = false) {
  private val SIZE_LIMIT:Long = Integer.parseInt(System.getProperty("nu.validator.servlet.max-file-size", "2097152"));

  if (!"1".equals(System.getProperty("nu.validator.servlet.read-local-log4j-properties"))) {
    PropertyConfigurator.configure(classOf[Main].getClassLoader().getResource("nu/validator/localentities/files/log4j.properties"));
  } else {
    PropertyConfigurator.configure(System.getProperty("nu.validator.servlet.log4j-properties", "log4j.properties"));
  }
  
  var server: Server = new Server
  var pool: QueuedThreadPool = new QueuedThreadPool
  pool.setMaxThreads(100);
  server.setThreadPool(pool);

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

object HTMLValidatorMain {

  def main(args: Array[String]): Unit = {
    val port = args.toList.headOption.map(_.toInt) getOrElse 8888
    val htmlval = new HTMLValidator(port)

    htmlval.start()

    println(">> press Enter to stop")
    scala.Console.readLine()

    htmlval.stop()
  }
}
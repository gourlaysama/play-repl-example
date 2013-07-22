package playrepl

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter._
import java.io.PrintWriter
import java.io.{PipedInputStream, PipedOutputStream}

object REPL {
  // a fresh REPL: an iteratee to receive commands, an enumerator to return results
  def getNew: (Iteratee[String, Unit], Enumerator[String]) = {
    // there has to be a better way to get the output of the REPL...
    val inStream = new PipedInputStream()
    val outStream = new PipedOutputStream(inStream)
    val print = new PrintWriter(outStream)
    val convert: Enumeratee[Array[Byte],String] = Enumeratee.map[Array[Byte]]{ a => new String(a) }
    val out = Enumerator.fromStream(inStream).through(convert)
    // ... but the above works, sadly.

    // get the correct classpath...
    val urls = java.lang.Thread.currentThread.getContextClassLoader match {
       case cl: java.net.URLClassLoader => cl.getURLs.toList
       case a => sys.error("oops: I was expecting an URLClassLoader, foud a " + a.getClass)
    }
    val classpath = urls map {_.toString}

    val settings = new Settings
    settings.classpath.value = classpath.distinct.mkString(java.io.File.pathSeparator)

    val interpreter = new IMain(settings, print){
      // ... and the correct classloader
      override protected def parentClassLoader = settings.getClass.getClassLoader()
    }

    val in = Iteratee.foreach[String] { code => interpreter.interpret(code) }.map { _ => 
      interpreter.close
      outStream.close 
    }
    (in, out)
  }
}

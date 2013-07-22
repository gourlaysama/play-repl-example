package playrepl

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter._
import java.io.{PrintWriter, PipedReader, PipedWriter, BufferedReader}
import java.io.{PipedInputStream, PipedOutputStream}

object REPL {
  // a fresh REPL: an iteratee to receive commands, an enumerator to return results
  def getNew: (Iteratee[String, Unit], Enumerator[String]) = {
    def buildSettings(): Settings = {
      // get the correct classpath...
      val urls = Play.classloader match {
        case cl: java.net.URLClassLoader => cl.getURLs.toList
        case a => sys.error("oops: I was expecting an URLClassLoader, foud a " + a.getClass)
      }
      val classpath = urls map {_.toString}

      val settings = new Settings
      settings.classpath.value = classpath.distinct.mkString(java.io.File.pathSeparator)

      // and the correct classLoader
      settings.embeddedDefaults(Play.classloader)

      settings
    }
    

    // there has to be a better way to get the output of the REPL...
    val inStream = new PipedInputStream()
    val outStream = new PipedOutputStream(inStream)
    val out = Enumerator.fromStream(inStream).map(a => new String(a))
    // ... but the above works, sadly.
    val print = new PrintWriter(outStream)

    // a cheap way to fool ILoop as to the input
    val inReader = new BufferedReader(new PipedReader(new PipedWriter()))
    val interpreter = new ILoop(inReader, print)

    val in = Iteratee.foreach[String] { code => interpreter.command(code) }.map { _ => 
      interpreter.closeInterpreter
      outStream.close
      inReader.close 
    }

    val settings = buildSettings()

    import play.api.libs.concurrent.Execution.Implicits._
    Akka.system.scheduler.scheduleOnce(100 milliseconds) {
      interpreter.process(settings)
    }

    (in, out)
  }
}

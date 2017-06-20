import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.event.Logging
import Console._

import scala.concurrent.Await
import scala.language.postfixOps


object Server extends App {


  val PORT = 8080

  implicit val actorSystem = ActorSystem("graphql-server")
  implicit val materializer = ActorMaterializer()

  import actorSystem.dispatcher
  import scala.concurrent.duration._



  logger("Starting GRAPHQL server...")


  //shutdown Hook
  scala.sys.addShutdownHook(() -> shutdown())

  def shutdown(): Unit = {

    logger("Terminating...", YELLOW)
    actorSystem.terminate()
    Await.result(actorSystem.whenTerminated, 30 seconds)
    logger("Terminated... Bye", RED)
  }

  def logger(message: String, color: String = GREEN): Unit = {
    println(color + message)
  }
}

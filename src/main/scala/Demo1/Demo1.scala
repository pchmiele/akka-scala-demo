package Demo1

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object Greeter {
  case object Hi
  case object Hello
}

class SimpleActor extends Actor with ActorLogging {
  def receive = {
    case Greeter.Hi => log.info("Hi!")
    case Greeter.Hello => log.info("Hello")
//    case _ => log.info("unkown message")
  }
}

object Main extends App {
  val system = ActorSystem("mySystem")
  val actor = system.actorOf(Props[SimpleActor], "Demo1")

  actor ! Greeter.Hi
  actor ! Greeter.Hello
  actor ! ""

  system.terminate()
}
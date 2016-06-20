package Demo3

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props}

import scala.concurrent.duration._

class Child extends Actor with ActorLogging {
  var state = 0

  def receive = {
    case e: Exception => throw e
    case x: Int => state = x
    case "status" => log.info("this.state = " + state)
  }
}

class Supervisor extends Actor with ActorLogging {
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ArithmeticException      => Resume
      case _: NullPointerException     => Restart
      case _: IllegalArgumentException => Stop
      case _: Exception                => Escalate
    }

  override def  preStart  {
    val child = context.actorOf(Props[Child], "child")
    log.info("child created with path: " + child.path)
  }

  def receive = {
    case _ => log.info("WAT!")
  }
}

object Main extends App {
  val system = ActorSystem("mySystem")
  val actor = system.actorOf(Props[Supervisor], "supervisor")
  val child = system.actorSelection("/user/supervisor/child")

  // how to save state in actor
  child ! "status"
  child ! 1
  child ! "status"

  // state should have previously set value
  child ! new ArithmeticException
  child ! "status"

  // state should have initial value
  child ! new NullPointerException
  child ! "status"

  // actor should no longer be available
  child ! new IllegalArgumentException
  child ! "status"

  system.terminate()

  val system2 = ActorSystem("mySystem")
  val actor2 = system2.actorOf(Props[Supervisor], "supervisor")
  val child2 = system2.actorSelection("/user/supervisor/child")

  child2 ! "status"
  child2 ! 1
  child2 ! "status"

  // actor should escalate it to his supervisor
  child2 ! new Exception
  child2 ! "status"
}
package Demo7

import akka.actor._

class Child extends Actor with ActorLogging {
  def receive = {
    case _ => log.info("Child received a message")
  }
}

class Parent extends Actor with ActorLogging {
  val child = context.actorOf(Props[Child], name = "child")
  context.watch(child)

  def receive = {
    case Terminated(child) => log.info("Child terminated")
    case _ => log.info("Parent received a message")
  }
}

object Main extends App {
  val system = ActorSystem("mySystem")
  val parent = system.actorOf(Props[Parent], name = "parent")

  val child = system.actorSelection("/user/parent/child")
  child ! PoisonPill
}
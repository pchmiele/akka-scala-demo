package Demo5

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case object Work

class Worker extends Actor with ActorLogging {
  def working : Receive = {
    case Work => log.info("I'm working")
      context.system.scheduler.scheduleOnce(1 second){
        context.become(break)
      }
  }

  def break: Receive = {
    case Work => log.info("I have a break now")
      context.system.scheduler.scheduleOnce(1 second){
        context.become(receive)
      }
    }

  def receive = {
    case Work => {
      log.info("Ok, I will start working in a sec")
      context.system.scheduler.scheduleOnce(1 second){
        context.become(working)
      }
    }
  }
}

object Main extends App {
  val system = ActorSystem("mySystem")
  val actor = system.actorOf(Props[Worker], "worker")

  for (i <- 1 to 20) {
    actor ! Work
    Thread.sleep(300)
  }

  system.terminate()
}
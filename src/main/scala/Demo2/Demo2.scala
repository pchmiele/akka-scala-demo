package Demo2

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object Work {
  case object Do
  case object Done
}

class Slave extends Actor with ActorLogging {
  def receive = {
    case Work.Do => {
      log.info("Yes master")
      sender() ! Work.Done
    }
  }
}

class Boss extends Actor with ActorLogging {
  override def preStart(): Unit = {
    val slave = context.actorOf(Props[Slave], "slave")
    log.info("Slave actor created. Slave path: " + slave.path)

    slave ! Work.Do
  }

  def receive = {
    case Work.Done => {
      log.info("Why so long?")
      context.stop(self)
    }
    case _ => log.info("Say what?")
  }
}

object Main extends App {
  val system = ActorSystem("mySystem")
  val actor = system.actorOf(Props[Boss], "boss")

  actor ! "asd"
}
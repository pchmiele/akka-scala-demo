package Demo4

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.routing.{BroadcastPool, RoundRobinGroup, RoundRobinPool}

case class Work(x: Int)

class Worker extends Actor with ActorLogging {
  def receive = {
    case Work(x) => log.info("Work to do: " + x)
  }
}

object Main extends App {
  val system = ActorSystem("mySystem")

  // Round robin pool example
  val roundRobinPoolRouter = system.actorOf(RoundRobinPool(5).props(Props[Worker]), "roundRobinPoolRouter")
  for (i <- 1 to 10) {
    roundRobinPoolRouter ! Work(i)
    Thread.sleep(300)
  }

  // Broadcast pool example
  val broadCastPoolRouter = system.actorOf(BroadcastPool(5).props(Props[Worker]), "broadCastPoolRouter")
  for (i <- 1 to 3) {
    broadCastPoolRouter ! Work(i)
    Thread.sleep(300)
  }

  // Round robin group example
  val worker1 = system.actorOf(Props[Worker], "worker1")
  val worker2 = system.actorOf(Props[Worker], "worker2")
  val worker3 = system.actorOf(Props[Worker], "worker3")
  val paths = List(worker1.path.toString, worker2.path.toString, worker3.path.toString)

  val roundRobinGroupRouter = system.actorOf(RoundRobinGroup(paths).props(), "roundRobinGroupRouter")

  for (i <- 1 to 10) {
    roundRobinGroupRouter ! Work(i)
    Thread.sleep(300)
  }

  system.terminate()
}